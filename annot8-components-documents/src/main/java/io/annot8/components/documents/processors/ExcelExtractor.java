/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import io.annot8.components.documents.data.WorksheetTable;
import io.annot8.conventions.PropertyKeys;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

@ComponentName("Excel (XLS and XLSX) Extractor")
@ComponentDescription("Extracts content from Excel (*.xls and *.xlsx) files into a table")
@ComponentTags({"documents", "excel", "xls", "xlsx", "extractor", "metadata"})
@SettingsClass(ExcelExtractor.Settings.class)
public class ExcelExtractor
    extends AbstractProcessorDescriptor<ExcelExtractor.Processor, ExcelExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(FileContent.class)
            .withProcessesContent(InputStreamContent.class)
            .withCreatesContent(TableContent.class);

    if (getSettings().isRemoveSourceContent())
      builder = builder.withDeletesContent(FileContent.class);

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents(FileContent.class)
          .filter(
              f ->
                  settings.getExtensions().isEmpty()
                      || settings
                          .getExtensions()
                          .contains(getExtension(f.getData().getName()).orElse("")))
          .forEach(
              f -> {
                try (InputStream fis = new FileInputStream(f.getData())) {
                  processInputStream(item, fis, f.getId());

                  if (settings.isRemoveSourceContent()) item.removeContent(f);
                } catch (Exception e) {
                  log().warn("Unable to process file {}", f.getData().getAbsolutePath(), e);
                }
              });

      item.getContents(InputStreamContent.class)
          .filter(this::acceptInputStream)
          .forEach(
              c -> {
                try {
                  processInputStream(item, c.getData(), c.getId());

                  if (settings.isRemoveSourceContent()) item.removeContent(c);
                } catch (Exception e) {
                  log().warn("Unable to process InputStream {}", c.getId(), e);
                }
              });

      return ProcessorResponse.ok();
    }

    public boolean acceptInputStream(InputStreamContent inputStream) {
      BufferedInputStream bis = new BufferedInputStream(inputStream.getData());
      FileMagic fm;
      try {
        fm = FileMagic.valueOf(bis);
      } catch (IOException e) {
        return false;
      }

      // FIXME: This only checks whether it is an OOXML, not that it is a Spreadsheet
      return FileMagic.OOXML == fm;
    }

    private void processInputStream(Item item, InputStream inputStream, String parentId)
        throws Exception {
      try (Workbook workbook = WorkbookFactory.create(inputStream)) {
        item.getProperties()
            .set(PropertyKeys.PROPERTY_KEY_VERSION, workbook.getSpreadsheetVersion().name());

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
          if (settings.getSkipSheets().contains(workbook.getSheetName(i))) {
            log().info("Skipping sheet {}", workbook.getSheetName(i));
            continue;
          }

          processSheet(
              item,
              workbook.getSheetAt(i),
              i,
              i == workbook.getActiveSheetIndex(),
              workbook.getSheetVisibility(i) == SheetVisibility.VISIBLE,
              parentId);
        }
      }
    }

    private Optional<String> getExtension(String filename) {
      return Optional.ofNullable(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
    }

    private void processSheet(
        Item item, Sheet sheet, int sheetIndex, boolean active, boolean visible, String parentId) {
      Table table = new WorksheetTable(sheet, settings.isFirstRowHeader(), settings.getSkipRows());

      item.createContent(TableContent.class)
          .withData(table)
          .withDescription(sheet.getSheetName())
          .withProperty(PropertyKeys.PROPERTY_KEY_PAGE, sheetIndex)
          .withProperty("active", active)
          .withProperty("visible", visible)
          .withPropertyIfPresent(PropertyKeys.PROPERTY_KEY_PARENT, Optional.ofNullable(parentId))
          .save();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> extensions = List.of("xls", "xlsx");
    private boolean removeSourceContent = true;
    private boolean firstRowHeader = true;
    private int skipRows = 0;
    private List<String> skipSheets = Collections.emptyList();

    public boolean validate() {
      return extensions != null && skipSheets != null;
    }

    @Description(
        "The list of file extensions on which this processor will act (case insensitive). If empty, then the processor will act on all files.")
    public List<String> getExtensions() {
      return extensions;
    }

    public void setExtensions(List<String> extensions) {
      this.extensions = extensions.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Description(
        value = "Should the source Content be removed after successful processing?",
        defaultValue = "true")
    public boolean isRemoveSourceContent() {
      return removeSourceContent;
    }

    public void setRemoveSourceContent(boolean removeSourceContent) {
      this.removeSourceContent = removeSourceContent;
    }

    @Description(
        value = "Is the first row of the spreadsheet a header row, to be used for column names?",
        defaultValue = "true")
    public boolean isFirstRowHeader() {
      return firstRowHeader;
    }

    public void setFirstRowHeader(boolean firstRowHeader) {
      this.firstRowHeader = firstRowHeader;
    }

    @Description(
        value =
            "The number of rows to skip (prior to reading the header, if firstRowHeader is true)",
        defaultValue = "0")
    public int getSkipRows() {
      return skipRows;
    }

    public void setSkipRows(int skipRows) {
      this.skipRows = skipRows;
    }

    @Description(value = "The name of any spreadsheets within a workbook which should be skipped")
    public List<String> getSkipSheets() {
      return skipSheets;
    }

    public void setSkipSheets(List<String> skipSheets) {
      this.skipSheets = skipSheets;
    }
  }
}
