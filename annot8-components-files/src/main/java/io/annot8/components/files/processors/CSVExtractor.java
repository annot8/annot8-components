/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import java.io.File;
import java.util.Optional;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.TableContent;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.components.files.content.CSVTable;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.exceptions.UnsupportedContentException;

@ProcessesContent(FileContent.class)
@CreatesContent(TableContent.class)
public class CSVExtractor extends AbstractComponent implements Processor {

  public static final String CSV_TABLE = "CSV_TABLE";
  private CSVExtractorSettings settings;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);
    Optional<CSVExtractorSettings> optional = context.getSettings(CSVExtractorSettings.class);
    if (optional.isPresent()) {
      settings = optional.get();
    } else {
      settings = new CSVExtractorSettings(false);
    }
  }

  @Override
  public ProcessorResponse process(Item item) throws Annot8Exception {
    item.getContents(FileContent.class)
        .filter(c -> c.getData().getAbsolutePath().endsWith(".csv"))
        .forEach(c -> createContent(item, c.getData()));
    return ProcessorResponse.ok();
  }

  private void createContent(Item item, File file) {
    try {
      item.create(TableContent.class)
          .withName(file.getName())
          .withData(new CSVTable(file, settings.hasHeaders()))
          .save();
    } catch (UnsupportedContentException | IncompleteException e) {
      log().error("Failed to create CSV content", e);
    }
  }
}
