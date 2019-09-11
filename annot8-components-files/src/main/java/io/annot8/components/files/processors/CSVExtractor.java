/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.TableContent;
import io.annot8.components.base.components.AbstractProcessor;
import io.annot8.components.files.content.CSVTable;
import io.annot8.core.capabilities.ContentCapability;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.exceptions.UnsupportedContentException;

import java.io.File;
import java.util.stream.Stream;

public class CSVExtractor extends AbstractProcessor<CSVExtractorSettings> {

  public static final String CSV_TABLE = "CSV_TABLE";
  public static final String PROPERTY_FILE = "file";

  private CSVExtractorSettings settings;

  @Override
  public void configure(CSVExtractorSettings settings)
      throws BadConfigurationException, MissingResourceException {

    if(settings == null){
      this.settings = new CSVExtractorSettings(false);
    }else{
      this.settings = settings;
    }
  }

  @Override
  public ProcessorResponse process(Item item) {
    item.getContents(FileContent.class)
        .filter(c -> c.getData().getAbsolutePath().endsWith(".csv"))
        .forEach(c -> createContent(item, c.getData()));
    return ProcessorResponse.ok();
  }

  private void createContent(Item item, File file) {
    try {
      item.createContent(TableContent.class)
          .withDescription(String.format("From CSV file[%s]", file.getName()))
          .withData(new CSVTable(file, settings.hasHeaders()))
          .withProperty(PROPERTY_FILE, file.getName())
          .save();
    } catch (UnsupportedContentException | IncompleteException e) {
      log().error("Failed to create CSV content", e);
    }
  }

  @Override
  public Stream<ContentCapability> processesContent() {
    return Stream.of(new ContentCapability(FileContent.class));
  }

  @Override
  public Stream<ContentCapability> createsContent() {
    return Stream.of(new ContentCapability(TableContent.class));
  }
}
