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

@ComponentName("Folder Source")
@ComponentDescription(
    "Treat each folder as an item with each file in the folder content for the item.")
@SettingsClass(FolderSource.Settings.class)
public class FolderSource
    extends AbstractSourceDescriptor<FolderSource.Source, FolderSource.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withCreatesContent(FileContent.class).build();
  }

  @Override
  protected Source createComponent(Context context, Settings settings) {
    return new Source(settings);
  }

  public static class Source extends AbstractSource {
    private final Queue<Path> folders = new LinkedList<>();
    private final Settings settings;

    public Source(FolderSource.Settings settings) {
      this.settings = settings;
      if (settings.isRecursive()) {
        settings
            .getPaths()
            .forEach(
                p -> {
                  try {
                    Files.walk(p)
                        .filter(f -> !p.equals(f))
                        .filter(Files::isDirectory)
                        .forEach(folders::add);
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
                    Files.list(p).filter(Files::isDirectory).forEach(folders::add);
                  } catch (Exception e) {
                    log().error("Unable to read files in path {}", p, e);
                  }
                });
      }

      log().info("{} folders to be processed", folders.size());
    }

    private boolean acceptExtension(Path p) {
      List<String> extensions = settings.getExtensions();
      if (extensions.isEmpty()) {
        return true;
      }

      return extensions.contains(
          com.google.common.io.Files.getFileExtension(p.toString()).toLowerCase());
    }

    @Override
    public SourceResponse read(ItemFactory itemFactory) {
      if (folders.isEmpty()) {
        return SourceResponse.done();
      }

      Path p = folders.poll();
      log().info("Processing {}", p);

      Item item = itemFactory.create();

      item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, p);

      try {
        Files.list(p)
            .filter(Files::isRegularFile)
            .filter(this::acceptExtension)
            .forEach(
                file -> {
                  Item child = item.createChild();
                  child.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, file);
                  child.createContent(FileContent.class).withData(file.toFile()).save();
                });
      } catch (Exception e) {
        log().error("Unable to read files in folder {}", p, e);
      }

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
