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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
  }
}
