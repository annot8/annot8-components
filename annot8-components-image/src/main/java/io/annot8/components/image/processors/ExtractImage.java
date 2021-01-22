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
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.InputStreamContent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

@ComponentName("Extract Image")
@ComponentDescription("Extract image content from InputStreams and Files for processing")
@SettingsClass(ExtractImage.Settings.class)
public class ExtractImage
    extends AbstractProcessorDescriptor<ExtractImage.Processor, ExtractImage.Settings> {
  @Override
  protected Processor createComponent(Context context, ExtractImage.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(InputStreamContent.class)
        .withProcessesContent(FileContent.class)
        .withCreatesContent(Image.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {

    private final List<String> extensions;
    private final boolean discardOriginal;

    public Processor(Settings settings) {
      extensions =
          settings.getFileExtensions().stream()
              .map(s -> s.trim().toLowerCase())
              .collect(Collectors.toList());
      discardOriginal = settings.isDiscardOriginal();
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Exception> exceptions = new ArrayList<>();

      // Process InputStreamContent
      item.getContents(InputStreamContent.class)
          .forEach(
              isc -> {
                BufferedImage img;
                try {
                  img = ImageIO.read(isc.getData());
                } catch (IOException e) {
                  log().error("Unable to read image from InputStream", e);
                  exceptions.add(e);
                  return;
                }

                if (discardOriginal) item.removeContent(isc);

                item.createContent(Image.class).withData(img).save();
              });

      // Process FileContent
      item.getContents(FileContent.class)
          .filter(
              fc ->
                  extensions.stream()
                      .anyMatch(ext -> fc.getData().getName().toLowerCase().endsWith("." + ext)))
          .forEach(
              fc -> {
                BufferedImage img;
                try {
                  img = ImageIO.read(fc.getData());
                } catch (IOException e) {
                  log().error("Unable to read image from File {}", fc.getData().getName(), e);
                  exceptions.add(e);
                  return;
                }

                if (discardOriginal) item.removeContent(fc);

                item.createContent(Image.class).withData(img).save();
              });

      if (exceptions.isEmpty()) {
        return ProcessorResponse.ok();
      } else {
        return ProcessorResponse.processingError(exceptions);
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> fileExtensions = List.of("jpg", "jpeg", "png", "gif", "bmp");
    private boolean discardOriginal = true;

    @Override
    public boolean validate() {
      return fileExtensions != null;
    }

    @Description("List of file extensions to accept when processing files")
    public List<String> getFileExtensions() {
      return fileExtensions;
    }

    public void setFileExtensions(List<String> fileExtensions) {
      this.fileExtensions = fileExtensions;
    }

    @Description("Should the original Content be discarded when an image is extracted?")
    public boolean isDiscardOriginal() {
      return discardOriginal;
    }

    public void setDiscardOriginal(boolean discardOriginal) {
      this.discardOriginal = discardOriginal;
    }
  }
}
