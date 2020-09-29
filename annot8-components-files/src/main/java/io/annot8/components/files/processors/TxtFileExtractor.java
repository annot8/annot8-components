/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

@ComponentName("Txt File Extractor")
@ComponentDescription("Extract text from a *.txt file and create Text content")
@SettingsClass(RemoveSourceContentSettings.class)
public class TxtFileExtractor
    extends AbstractProcessorDescriptor<TxtFileExtractor.Processor, RemoveSourceContentSettings> {

  @Override
  protected Processor createComponent(Context context, RemoveSourceContentSettings settings) {
    return new Processor(settings.isRemoveSourceContent());
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(FileContent.class)
            .withCreatesContent(Text.class);

    if (getSettings().isRemoveSourceContent()) {
      builder = builder.withDeletesContent(FileContent.class);
    }

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    private final boolean removeSourceContent;

    public Processor(boolean removeSourceContent) {
      this.removeSourceContent = removeSourceContent;
    }

    @Override
    public ProcessorResponse process(Item item) {

      item.getContents(FileContent.class)
          .filter(f -> f.getData().getName().endsWith(".txt"))
          .forEach(
              f -> {
                try {
                  File file = f.getData();
                  String data =
                      new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
                  item.createContent(Text.class)
                      .withDescription("Text from " + f.getId())
                      .withData(data)
                      .save();

                  // If processed, then remove it our item so it doesn't get reprocessed
                  if (removeSourceContent) item.removeContent(f);

                } catch (Exception e) {
                  log().warn("Unable to process file {}", f.getData().getAbsolutePath());
                  log().debug("Unable to process file", e);
                }
              });

      // Always carry on it
      return ProcessorResponse.ok();
    }
  }
}
