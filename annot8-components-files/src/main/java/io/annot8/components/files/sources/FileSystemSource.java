/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.annot8.core.components.responses.SourceResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.ItemFactory;
import io.annot8.core.exceptions.Annot8RuntimeException;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;

public class FileSystemSource extends AbstractFileSystemSource {

  private final WatchService watchService;

  private final Set<WatchKey> watchKeys = new HashSet<>();

  private final Set<Path> initialFiles = new HashSet<>();

  public FileSystemSource() {
    try {
      watchService = FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      throw new Annot8RuntimeException("Unable to initialize WatchService", e);
    }
  }

  @Override
  public void configure(final Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    // Unregister existing watchers
    watchKeys.forEach(WatchKey::cancel);
    watchKeys.clear();

    initialFiles.clear();

    try {
      Path p = getSettings().getRootFolder();

      if (getSettings().isRecursive()) {
        Files.walkFileTree(
            p,
            new SimpleFileVisitor<>() {
              @Override
              public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr)
                  throws IOException {
                registerDirectory(getSettings(), dir);
                return FileVisitResult.CONTINUE;
              }
            });
      } else {
        registerDirectory(getSettings(), p);
      }

      addFilesFromDir(getSettings(), p.toFile());
    } catch (IOException ioe) {
      throw new BadConfigurationException(
          "Unable to register folder or sub-folder with watch service", ioe);
    }
  }

  private void registerDirectory(FileSystemSourceSettings settings, Path path) throws IOException {
    WatchKey key;
    if (settings.isReprocessOnModify()) {
      key = path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
    } else {
      key = path.register(watchService, ENTRY_CREATE);
    }

    watchKeys.add(key);
  }

  private void addFilesFromDir(FileSystemSourceSettings settings, File dir) {
    File[] files = dir.listFiles();

    if (files == null) {
      return;
    }

    for (File file : files) {
      if (!file.isDirectory()) {
        Path path = file.toPath();

        if (getAcceptedFilePatterns().isEmpty()) {
          initialFiles.add(path);
        } else {
          for (Pattern p : getAcceptedFilePatterns()) {
            Matcher m = p.matcher(path.getFileName().toString());
            if (m.matches()) {
              initialFiles.add(path);
              break;
            }
          }
        }
      } else if (settings.isRecursive()) {
        addFilesFromDir(settings, file);
      }
    }
  }

  @Override
  public SourceResponse read(ItemFactory itemFactory) {
    if (!initialFiles.isEmpty()) {
      initialFiles.forEach(path -> createItem(itemFactory, path));
      initialFiles.clear();

      if (!getSettings().isWatching()) {
        watchKeys.forEach(WatchKey::cancel);
        watchKeys.clear();
        return SourceResponse.done();
      }
    }

    boolean read = false;
    WatchKey key;
    while ((key = watchService.poll()) != null) {
      for (WatchEvent<?> event : key.pollEvents()) {
        if (createItem(itemFactory, ((WatchEvent<Path>) event).context())) {
          read = true;
        }
      }

      key.reset();
    }

    if (read) {
      return SourceResponse.ok();
    }

    return SourceResponse.empty();
  }
}
