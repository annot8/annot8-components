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
import io.annot8.api.data.Item;
import io.annot8.api.data.ItemFactory;
import io.annot8.api.exceptions.Annot8RuntimeException;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractSource;
import io.annot8.common.components.AbstractSourceDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.conventions.PropertyKeys;
import jakarta.json.bind.annotation.JsonbCreator;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("File System Source")
@ComponentDescription("Provides items from the local file system")
@SettingsClass(FileSystemSource.Settings.class)
public class FileSystemSource
    extends AbstractSourceDescriptor<FileSystemSource.Source, FileSystemSource.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withCreatesContent(FileContent.class).build();
  }

  @Override
  protected Source createComponent(Context context, Settings settings) {
    return new Source(settings);
  }

  public static class Source extends AbstractSource {
    private final WatchService watchService;
    private final Settings settings;

    private final Set<Path> initialFiles = new HashSet<>();
    private final Set<Path> queue = Collections.synchronizedSet(new HashSet<>());

    public Source(Settings settings) {
      this.settings = settings;

      // Initialize watch server, but only if we're watching folders
      if (settings.isWatching()) {
        try {
          watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
          throw new Annot8RuntimeException("Unable to initialize WatchService", e);
        }
      } else {
        watchService = null;
      }

      // Register folders for watch service and add initial files
      try {
        Path p = settings.getRootFolder();

        if (settings.isRecursive()) {
          Files.walkFileTree(
              p,
              new SimpleFileVisitor<>() {
                // Watch directory
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr)
                    throws IOException {
                  registerDirectory(dir);
                  return FileVisitResult.CONTINUE;
                }

                // Add file to list
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                  if (acceptFile(file, settings)) initialFiles.add(file);

                  return FileVisitResult.CONTINUE;
                }
              });
        } else {
          // Watch directory
          registerDirectory(p);

          // Add files to list
          Files.list(p)
              .filter(Files::isRegularFile)
              .filter(file -> acceptFile(file, settings))
              .forEach(initialFiles::add);
        }

      } catch (IOException ioe) {
        throw new BadConfigurationException(
            "Unable to register folder or sub-folder with watch service or list initial files",
            ioe);
      }

      log().info("{} files identified for initial processing", initialFiles.size());
    }

    private void registerDirectory(Path path) throws IOException {
      // Only register a directory if we're going to watch it
      if (settings.isWatching()) {
        // If we want to reprocess on modify, then also watch the MODIFY event
        if (settings.isReprocessOnModify()) {
          log().info("Registering {} with watch service for CREATE and MODIFY events", path);
          path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
        } else {
          log().info("Registering {} with watch service for CREATE events", path);
          path.register(watchService, ENTRY_CREATE);
        }
      }
    }

    public static boolean acceptFile(Path file, Settings settings) {
      // If no patterns, then accept everything
      if (settings.getAcceptedFileNamePatterns().isEmpty()) return true;

      // See if the filename matches any of the patterns
      boolean matched =
          settings.getAcceptedFileNamePatterns().stream()
              .map(p -> p.matcher(file.getFileName().toString()))
              .anyMatch(Matcher::matches);
      return matched != settings.isNegateAcceptedFileNamePatterns();
    }

    @Override
    public SourceResponse read(ItemFactory itemFactory) {
      long read = 0;

      // Process any initial files
      if (!initialFiles.isEmpty()) {
        read +=
            initialFiles.stream()
                .filter(queue::add)
                .peek(file -> createItem(itemFactory, file, settings.getDelay()))
                .mapToLong(p -> 1L)
                .sum();

        initialFiles.clear();
      }

      // Not watching, so we don't need to check the watch keys and can return DONE at this point if
      // queue is empty
      if (watchService == null || !settings.isWatching()) {
        return queue.isEmpty() ? SourceResponse.done() : SourceResponse.empty();
      }

      // Watching, so check the watch keys for more files
      WatchKey key;
      while ((key = watchService.poll()) != null) {
        Path dir = (Path) key.watchable();
        List<WatchEvent<?>> events = key.pollEvents();

        // Watch new directories
        events.stream()
            .filter(e -> e.kind() == ENTRY_CREATE)
            .map(event -> dir.resolve(((WatchEvent<Path>) event).context()))
            .filter(Files::isDirectory)
            .forEach(
                p -> {
                  try {
                    registerDirectory(p);
                  } catch (IOException e) {
                    log().error("Unable to watch new folder {}", p);
                  }
                });

        // Process each event and create an Item from it
        read +=
            events.stream()
                .map(event -> dir.resolve(((WatchEvent<Path>) event).context()))
                .filter(Files::isRegularFile)
                .filter(file -> acceptFile(file, settings))
                .filter(queue::add) // Check that we're not already about to process this, as we may
                // receive multiple events for the same file
                .peek(file -> createItem(itemFactory, file, settings.getDelay()))
                .mapToLong(p -> 1L)
                .sum();

        key.reset();
      }

      // If we (successfully) read any files, then return OK. Otherwise EMPTY
      return read > 0 ? SourceResponse.ok() : SourceResponse.empty();
    }

    private void createItem(ItemFactory itemFactory, Path path, long delay) {
      log().debug("Scheduling item creation for {} after delay of {} milliseconds", path, delay);

      // Create an item, after a delay as required

      new Timer()
          .schedule(
              new TimerTask() {
                @Override
                public void run() {
                  log().debug("Creating item from {}", path);
                  itemFactory.create(i -> createFileContent(i, path));
                  queue.remove(path);
                }
              },
              delay);
    }

    private void createFileContent(Item item, Path path) {
      item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, path);
      item.getProperties()
          .set(PropertyKeys.PROPERTY_KEY_ACCESSEDAT, Instant.now().getEpochSecond());

      item.createContent(FileContent.class)
          .withDescription("File " + path.toString())
          .withData(path.toFile())
          .save();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Path rootFolder = Paths.get(".");
    private boolean watching = true;
    private boolean recursive = true;
    private boolean reprocessOnModify = true;
    private Set<Pattern> acceptedFileNamePatterns = new HashSet<>();
    private boolean negateAcceptedFileNamePatterns = false;
    private long delay = 0L;

    @JsonbCreator
    public Settings() {
      // Do nothing
    }

    public Settings(final Path rootFolder) {
      this.rootFolder = rootFolder;
    }

    @Description(value = "Root folder to read from", defaultValue = ".")
    public Path getRootFolder() {
      return rootFolder;
    }

    public void setRootFolder(final Path rootFolder) {
      this.rootFolder = rootFolder;
    }

    @Description(value = "Should the folder be read recursively", defaultValue = "true")
    public boolean isRecursive() {
      return recursive;
    }

    public void setRecursive(boolean recursive) {
      this.recursive = recursive;
    }

    @Description(value = "Should files be reprocessed if they are modified", defaultValue = "true")
    public boolean isReprocessOnModify() {
      return reprocessOnModify;
    }

    public void setReprocessOnModify(boolean reprocessOnModify) {
      this.reprocessOnModify = reprocessOnModify;
    }

    @Description("Accepted file name patterns")
    public Set<Pattern> getAcceptedFileNamePatterns() {
      return acceptedFileNamePatterns;
    }

    public void setAcceptedFileNamePatterns(Set<Pattern> acceptedFileNamePatterns) {
      this.acceptedFileNamePatterns = acceptedFileNamePatterns;
    }

    @Description(
        value =
            "If true, then the list of accepted file name patterns is treated as a reject list rather than an accept list",
        defaultValue = "false")
    public boolean isNegateAcceptedFileNamePatterns() {
      return negateAcceptedFileNamePatterns;
    }

    public void setNegateAcceptedFileNamePatterns(boolean negateAcceptedFileNamePatterns) {
      this.negateAcceptedFileNamePatterns = negateAcceptedFileNamePatterns;
    }

    @Description(
        value = "Should the folder be watched for changes (true), or just scanned once (false)",
        defaultValue = "true")
    public boolean isWatching() {
      return watching;
    }

    public void setWatching(boolean watching) {
      this.watching = watching;
    }

    @Description(
        value =
            "The length of delay to introduce between the file being detected and the file being processed - can be used to avoid partially copied files being picked up",
        defaultValue = "0")
    public long getDelay() {
      return delay;
    }

    public void setDelay(long delay) {
      this.delay = delay;
    }

    @Override
    public boolean validate() {
      return rootFolder != null && acceptedFileNamePatterns != null && delay >= 0;
    }
  }
}
