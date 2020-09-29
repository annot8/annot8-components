/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.exceptions.UnsupportedContentException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.TableContent;
import io.annot8.components.files.content.CSVTable;
import java.io.File;

@ComponentName("CSV Extractor")
@ComponentDescription("Extract CSV files into a Table content")
@SettingsClass(CSVExtractor.Settings.class)
public class CSVExtractor
    extends AbstractProcessorDescriptor<CSVExtractor.Processor, CSVExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.isHasHeaders(), settings.isRemoveSourceContent());
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(FileContent.class)
            .withCreatesContent(TableContent.class);

    if (getSettings().isRemoveSourceContent()) {
      builder = builder.withDeletesContent(FileContent.class);
    }

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    public static final String PROPERTY_FILE = "file";

    private final boolean hasHeaders;
    private final boolean removeSourceContent;

    public Processor(boolean hasHeaders, boolean removeSourceContent) {
      this.hasHeaders = hasHeaders;
      this.removeSourceContent = removeSourceContent;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents(FileContent.class)
          .filter(c -> c.getData().getAbsolutePath().endsWith(".csv"))
          .forEach(
              c -> {
                File file = c.getData();

                try {
                  item.createContent(TableContent.class)
                      .withDescription(String.format("From CSV file[%s]", file.getName()))
                      .withData(new CSVTable(file, hasHeaders))
                      .withProperty(PROPERTY_FILE, file.getName())
                      .save();

                  // If processed, then remove it our item so it doesn't get reprocessed
                  if (removeSourceContent) item.removeContent(c);
                } catch (UnsupportedContentException | IncompleteException e) {
                  log().error("Failed to create CSV content", e);
                }
              });
      return ProcessorResponse.ok();
    }

    private void createContent(Item item, File file) {
      try {
        item.createContent(TableContent.class)
            .withDescription(String.format("From CSV file[%s]", file.getName()))
            .withData(new CSVTable(file, hasHeaders))
            .withProperty(PROPERTY_FILE, file.getName())
            .save();
      } catch (UnsupportedContentException | IncompleteException e) {
        log().error("Failed to create CSV content", e);
      }
    }
  }

  public static class Settings extends RemoveSourceContentSettings {
    private boolean hasHeaders;

    @Override
    public boolean validate() {
      return true;
    }

    @Description("Does the CSV file have headers (true) or not (false)")
    public boolean isHasHeaders() {
      return hasHeaders;
    }

    public void setHasHeaders(boolean hasHeaders) {
      this.hasHeaders = hasHeaders;
    }
  }
}
