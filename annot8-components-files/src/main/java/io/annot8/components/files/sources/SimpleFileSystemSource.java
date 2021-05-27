/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.SourceResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.data.ItemFactory;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractSource;
import io.annot8.common.components.AbstractSourceDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.conventions.PropertyKeys;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

@ComponentName("Simple File System Source")
@ComponentDescription(
    "Take a list of folders and processes files in them, without watching for changes or new files")
@SettingsClass(SimpleFileSystemSource.Settings.class)
public class SimpleFileSystemSource
    extends AbstractSourceDescriptor<
        SimpleFileSystemSource.Source, SimpleFileSystemSource.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withCreatesContent(FileContent.class).build();
  }

  @Override
  protected Source createComponent(Context context, Settings settings) {
    return new Source(settings);
  }

  public static class Source extends AbstractSource {
    private final Queue<Path> files = new LinkedList<>();

    public Source(SimpleFileSystemSource.Settings settings) {
      if (settings.isRecursive()) {
        settings
            .getPaths()
            .forEach(
                p -> {
                  try {
                    Files.walk(p)
                        .filter(Files::isRegularFile)
                        .filter(path -> acceptExtension(settings.getExtensions(), path))
                        .sorted(sortPaths(settings.getFileOrder()))
                        .forEach(files::add);
                  } catch (Exception e) {
                    log().error("Unable to read files recursively in path {}", p, e);
                  }
                });
      } else {
        settings
            .getPaths()
            .forEach(
                p -> {
                  try {
                    Files.list(p)
                        .filter(Files::isRegularFile)
                        .filter(path -> acceptExtension(settings.getExtensions(), path))
                        .sorted(sortPaths(settings.getFileOrder()))
                        .forEach(files::add);
                  } catch (Exception e) {
                    log().error("Unable to read files in path {}", p, e);
                  }
                });
      }

      log().info("{} files to be processed", files.size());
    }

    private static boolean acceptExtension(List<String> extensions, Path p) {
      if (extensions.isEmpty()) return true;

      return extensions.contains(
          com.google.common.io.Files.getFileExtension(p.toString()).toLowerCase());
    }

    private static Comparator<Path> sortPaths(FileOrder fileOrder) {
      switch (fileOrder) {
        case CREATED_DATE_EARLIEST_TO_LATEST:
          return (p1, p2) -> {
            try {
              BasicFileAttributes bfa1 = Files.readAttributes(p1, BasicFileAttributes.class);
              BasicFileAttributes bfa2 = Files.readAttributes(p2, BasicFileAttributes.class);

              return bfa1.creationTime().compareTo(bfa2.creationTime());
            } catch (IOException ioe) {
              return 0;
            }
          };
        case CREATED_DATE_LATEST_TO_EARLIEST:
          return (p1, p2) -> {
            try {
              BasicFileAttributes bfa1 = Files.readAttributes(p1, BasicFileAttributes.class);
              BasicFileAttributes bfa2 = Files.readAttributes(p2, BasicFileAttributes.class);

              return bfa2.creationTime().compareTo(bfa1.creationTime());
            } catch (IOException ioe) {
              return 0;
            }
          };
        case MODIFIED_DATE_EARLIEST_TO_LATEST:
          return (p1, p2) -> {
            try {
              BasicFileAttributes bfa1 = Files.readAttributes(p1, BasicFileAttributes.class);
              BasicFileAttributes bfa2 = Files.readAttributes(p2, BasicFileAttributes.class);

              return bfa1.lastModifiedTime().compareTo(bfa2.lastModifiedTime());
            } catch (IOException ioe) {
              return 0;
            }
          };
        case MODIFIED_DATE_LATEST_TO_EARLIEST:
          return (p1, p2) -> {
            try {
              BasicFileAttributes bfa1 = Files.readAttributes(p1, BasicFileAttributes.class);
              BasicFileAttributes bfa2 = Files.readAttributes(p2, BasicFileAttributes.class);

              return bfa2.lastModifiedTime().compareTo(bfa1.lastModifiedTime());
            } catch (IOException ioe) {
              return 0;
            }
          };
        case NAME_A_TO_Z:
          return Comparator.comparing(p -> p.getFileName().toString());
        case NAME_Z_TO_A:
          return Comparator.comparing((Path p) -> p.getFileName().toString()).reversed();
        case SIZE_SMALL_TO_LARGE:
          return Comparator.comparing(p -> p.toFile().length());
        case SIZE_LARGE_TO_SMALL:
          return Comparator.comparing((Path p) -> p.toFile().length()).reversed();
      }

      return Comparator.comparing(Function.identity());
    }

    @Override
    public SourceResponse read(ItemFactory itemFactory) {
      if (files.isEmpty()) {
        return SourceResponse.done();
      }

      Path p = files.poll();
      log().info("Processing {}", p);

      Item item = itemFactory.create();

      item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, p);

      item.createContent(FileContent.class).withData(p.toFile()).save();

      return SourceResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<Path> paths = new ArrayList<>();
    private List<String> extensions = new ArrayList<>();
    private boolean recursive = true;
    private FileOrder fileOrder = FileOrder.NAME_A_TO_Z;

    @Override
    public boolean validate() {
      return extensions != null && paths != null && !paths.isEmpty();
    }

    @Description("List of paths to process")
    public List<Path> getPaths() {
      return paths;
    }

    public void setPaths(List<Path> paths) {
      this.paths = paths;
    }

    @Description("List of file extensions to accept (accepts all files if no extensions are given)")
    public List<String> getExtensions() {
      return extensions;
    }

    public void setExtensions(List<String> extensions) {
      this.extensions = extensions.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Description("Should we process paths recursively?")
    public boolean isRecursive() {
      return recursive;
    }

    public void setRecursive(boolean recursive) {
      this.recursive = recursive;
    }

    @Description("The order in which files will be processed")
    public FileOrder getFileOrder() {
      return fileOrder;
    }

    public void setFileOrder(FileOrder fileOrder) {
      this.fileOrder = fileOrder;
    }
  }

  public enum FileOrder {
    CREATED_DATE_EARLIEST_TO_LATEST,
    CREATED_DATE_LATEST_TO_EARLIEST,
    MODIFIED_DATE_EARLIEST_TO_LATEST,
    MODIFIED_DATE_LATEST_TO_EARLIEST,
    NAME_A_TO_Z,
    NAME_Z_TO_A,
    SIZE_LARGE_TO_SMALL,
    SIZE_SMALL_TO_LARGE
  }
}
