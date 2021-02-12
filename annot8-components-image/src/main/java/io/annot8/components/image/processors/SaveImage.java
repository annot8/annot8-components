/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

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
import io.annot8.common.data.content.Image;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@ComponentName("Save Image")
@ComponentDescription("Save image content to disk")
@SettingsClass(SaveImage.Settings.class)
public class SaveImage
    extends AbstractProcessorDescriptor<SaveImage.Processor, SaveImage.Settings> {
  @Override
  protected Processor createComponent(Context context, SaveImage.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesContent(Image.class).build();
  }

  public static class Processor extends AbstractProcessor {

    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      if (item.getContents(Image.class).count() == 0L) return ProcessorResponse.ok();

      Path p = settings.getOutputFolder().resolve(item.getId());
      try {
        Files.createDirectories(p);
      } catch (IOException e) {
        log().error("Could not create output directory", e);
        return ProcessorResponse.processingError(e);
      }

      List<Exception> exceptions = new ArrayList<>();

      item.getContents(Image.class)
          .forEach(
              i -> {
                Path pFile;
                if (settings.getFileType() == FileType.PNG) {
                  pFile = p.resolve(i.getId() + ".png");
                } else {
                  pFile = p.resolve(i.getId() + ".jpg");
                }

                try (FileOutputStream fos = new FileOutputStream(pFile.toFile())) {
                  if (settings.getFileType() == FileType.PNG) {
                    i.saveAsPng(fos);
                  } else {
                    i.saveAsJpg(fos);
                  }
                  log().info("Image content saved to {}", pFile);
                } catch (IOException e) {
                  log().error("Unable to write content {} to disk {}", i.getId(), pFile, e);
                  exceptions.add(e);
                }
              });

      if (exceptions.isEmpty()) return ProcessorResponse.ok();

      return ProcessorResponse.processingError(exceptions);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private FileType fileType = FileType.JPG;
    private Path outputFolder = Path.of(".");

    @Override
    public boolean validate() {
      return fileType != null && outputFolder != null;
    }

    @Description("The file type to use when persisting images")
    public FileType getFileType() {
      return fileType;
    }

    public void setFileType(FileType fileType) {
      this.fileType = fileType;
    }

    @Description(
        "The root folder to output images in to - each Item will create a separate folder within this folder, and images will be saved into that folder")
    public Path getOutputFolder() {
      return outputFolder;
    }

    public void setOutputFolder(Path outputFolder) {
      this.outputFolder = outputFolder;
    }
  }

  public enum FileType {
    JPG,
    PNG
  }
}
