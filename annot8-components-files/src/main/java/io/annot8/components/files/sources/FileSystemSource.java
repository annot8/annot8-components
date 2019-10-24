/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.SourceResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.ItemFactory;
import io.annot8.api.exceptions.Annot8RuntimeException;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.common.components.AbstractSourceDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("File System Source")
@ComponentDescription("Provides items from the local file system")
@SettingsClass(FileSystemSourceSettings.class)
public class FileSystemSource
    extends AbstractSourceDescriptor<FileSystemSource.Source, FileSystemSourceSettings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withCreatesContent(FileContent.class).build();
  }

  @Override
  protected Source createComponent(Context context, FileSystemSourceSettings settings) {
    return new Source(settings);
  }

  public static class Source extends AbstractFileSystemSource {

    private final WatchService watchService;

    private final Set<WatchKey> watchKeys = new HashSet<>();

    private final Set<Path> initialFiles = new HashSet<>();

    public Source(FileSystemSourceSettings settings) {
      super(settings);

      try {
        watchService = FileSystems.getDefault().newWatchService();
      } catch (IOException e) {
        throw new Annot8RuntimeException("Unable to initialize WatchService", e);
      }

      // TODO: This probably shouldnt' happen in the constructor but on the first read()

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

    private void registerDirectory(FileSystemSourceSettings settings, Path path)
        throws IOException {
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
}
