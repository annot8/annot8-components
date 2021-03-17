/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.conventions.PropertyKeys;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ComponentName("Move Source File")
@ComponentDescription("Move (or copy) the source file to a different directory")
@SettingsClass(MoveSourceFile.Settings.class)
public class MoveSourceFile
    extends AbstractProcessorDescriptor<MoveSourceFile.Processor, MoveSourceFile.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      // Get source information from item
      Optional<Path> source = getItemSource(item);

      if (source.isEmpty()) {
        log().debug("Could not find or parse source for item {}", item.getId());
        return ProcessorResponse.ok();
      }

      // Get target destination
      Path target =
          getTargetPath(
              source.get(),
              settings.getRootOutputFolder(),
              settings.getBasePaths(),
              settings.isFlatten());

      // Create output folder for item
      try {
        Files.createDirectories(target.getParent());
      } catch (IOException e) {
        log().error("Unable to create directory for item {}", item.getId(), e);
        return ProcessorResponse.itemError(e);
      }

      List<CopyOption> copyOptions = new ArrayList<>();
      if (settings.isCopyOriginalFile()) copyOptions.add(StandardCopyOption.COPY_ATTRIBUTES);
      if (settings.isReplaceExisting()) copyOptions.add(StandardCopyOption.REPLACE_EXISTING);

      if (settings.isCopyOriginalFile()) {
        // Copy file
        try {
          Files.copy(source.get(), target, copyOptions.toArray(new CopyOption[0]));
        } catch (IOException e) {
          log()
              .error(
                  "Unable to copy source file {} to {} for item {}",
                  source.get(),
                  target,
                  item.getId(),
                  e);
          return ProcessorResponse.itemError(e);
        }

        log().info("Source file {} copied to {} for item {}", source.get(), target, item.getId());
      } else {
        // Move file
        try {
          Files.move(source.get(), target, copyOptions.toArray(new CopyOption[0]));
        } catch (IOException e) {
          log()
              .error(
                  "Unable to move source file {} to {} for item {}",
                  source.get(),
                  target,
                  item.getId(),
                  e);
          return ProcessorResponse.itemError(e);
        }

        log().info("Source file {} moved to {} for item {}", source.get(), target, item.getId());
      }

      if (settings.isUpdateSource()) {
        item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, target);
      }

      return ProcessorResponse.ok();
    }

    protected static Optional<Path> getItemSource(Item item) {
      Optional<Object> o = item.getProperties().get(PropertyKeys.PROPERTY_KEY_SOURCE);
      if (o.isEmpty()) {
        return Optional.empty();
      }

      Object source = o.get();

      Path p;
      if (source instanceof Path) {
        p = (Path) source;
      } else if (source instanceof File) {
        p = ((File) source).toPath();
      } else if (source instanceof String) {
        p = Path.of((String) source);
      } else if (source instanceof URI) {
        p = Path.of((URI) source);
      } else {
        return Optional.empty();
      }

      return Optional.of(p);
    }

    protected static Path getTargetPath(
        Path source, Path rootOutputFolder, List<Path> baseSourceFolders, boolean flatten) {
      Path cleanSource;
      if (flatten) {
        cleanSource = source.getFileName();
      } else {
        cleanSource =
            baseSourceFolders.stream()
                .filter(source::startsWith)
                .findFirst()
                .map(path -> path.relativize(source))
                .orElse(source);
      }

      return Path.of(rootOutputFolder.toString(), cleanSource.toString());
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Path rootOutputFolder = Path.of(".");
    private List<Path> basePaths = Collections.emptyList();
    private boolean copyOriginalFile = false;
    private boolean replaceExisting = false;
    private boolean flatten = false;
    private boolean updateSource = false;

    @Override
    public boolean validate() {
      return rootOutputFolder != null && basePaths != null;
    }

    @Description(value = "The root folder in which to save files", defaultValue = ".")
    public Path getRootOutputFolder() {
      return rootOutputFolder;
    }

    public void setRootOutputFolder(Path rootOutputFolder) {
      this.rootOutputFolder = rootOutputFolder;
    }

    @Description(
        "If the source path of any Item begins with any of these paths, then it will be truncated")
    public List<Path> getBasePaths() {
      return basePaths;
    }

    public void setBasePaths(List<Path> basePaths) {
      this.basePaths = basePaths;
    }

    @Description(
        value = "If true, then the source file is copied instead of moved",
        defaultValue = "false")
    public boolean isCopyOriginalFile() {
      return copyOriginalFile;
    }

    public void setCopyOriginalFile(boolean copyOriginalFile) {
      this.copyOriginalFile = copyOriginalFile;
    }

    @Description(
        value =
            "If true, then any existing files will be replaced. If false, then an error will be thrown if the target file already exists.",
        defaultValue = "false")
    public boolean isReplaceExisting() {
      return replaceExisting;
    }

    public void setReplaceExisting(boolean replaceExisting) {
      this.replaceExisting = replaceExisting;
    }

    @Description(
        value =
            "If true, then all files are moved into the same top level folder and the base path is ignored",
        defaultValue = "false")
    public boolean isFlatten() {
      return flatten;
    }

    public void setFlatten(boolean flatten) {
      this.flatten = flatten;
    }

    @Description(
        value =
            "If true, then the source property of the Item is updated following a successful move",
        defaultValue = "false")
    public boolean isUpdateSource() {
      return updateSource;
    }

    public void setUpdateSource(boolean updateSource) {
      this.updateSource = updateSource;
    }
  }
}
