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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ComponentName("CSV Extractor")
@ComponentDescription("Extract CSV files into a Table content")
@SettingsClass(CSVExtractor.Settings.class)
public class CSVExtractor
    extends AbstractProcessorDescriptor<CSVExtractor.Processor, CSVExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(
        settings.isHasHeaders(), settings.isRemoveSourceContent(), settings.getExtensions());
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
    private final List<String> extensions;

    public Processor(boolean hasHeaders, boolean removeSourceContent, List<String> extensions) {
      this.hasHeaders = hasHeaders;
      this.removeSourceContent = removeSourceContent;
      this.extensions = extensions;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents(FileContent.class)
          .filter(
              f ->
                  extensions.isEmpty()
                      || extensions.contains(getExtension(f.getData().getName()).orElse("")))
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

    private Optional<String> getExtension(String filename) {
      return Optional.ofNullable(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
    }
  }

  public static class Settings extends RemoveSourceContentSettings {
    private boolean hasHeaders;
    private List<String> extensions = List.of("csv");

    @Override
    public boolean validate() {
      return super.validate() && extensions != null;
    }

    @Description("Does the CSV file have headers (true) or not (false)")
    public boolean isHasHeaders() {
      return hasHeaders;
    }

    public void setHasHeaders(boolean hasHeaders) {
      this.hasHeaders = hasHeaders;
    }

    @Description(
        "The list of file extensions on which this processor will act (case insensitive). If empty, then the processor will act on all files.")
    public List<String> getExtensions() {
      return extensions;
    }

    public void setExtensions(List<String> extensions) {
      this.extensions = extensions.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
  }
}
