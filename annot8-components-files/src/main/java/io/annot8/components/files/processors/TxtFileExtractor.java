/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.NoSettings;
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
public class TxtFileExtractor
    extends AbstractProcessorDescriptor<TxtFileExtractor.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(FileContent.class)
        .withCreatesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {

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

                  // If we processed it ... lets remove it from our item
                  // so it doesn't get reprocessed
                  item.removeContent(f);

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
