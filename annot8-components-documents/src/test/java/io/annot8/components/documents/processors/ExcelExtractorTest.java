/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.api.helpers.WithDescription;
import io.annot8.api.helpers.WithId;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ExcelExtractorTest {
  @Test
  public void testXlsx() {
    URL resource = DocxExtractorTest.class.getResource("testSpreadsheet.xlsx");
    File f;
    try {
      f = Paths.get(resource.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    TestItem item = new TestItem();
    item.createContent(FileContent.class).withData(f).save();

    testItem(item);
  }

  @Test
  public void testXls() {
    URL resource = DocxExtractorTest.class.getResource("testSpreadsheet.xls");
    File f;
    try {
      f = Paths.get(resource.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    TestItem item = new TestItem();
    item.createContent(FileContent.class).withData(f).save();

    testItem(item);
  }

  private void testItem(Item item) {
    ExcelExtractor.Settings s = new ExcelExtractor.Settings();
    s.setExtensions(List.of("xlsx", "xls"));
    s.setFirstRowHeader(true);
    s.setRemoveSourceContent(true);

    String source =
        item.getContents(FileContent.class).findFirst().orElseThrow().getData().getName();

    try (ExcelExtractor.Processor p = new ExcelExtractor.Processor(s)) {
      ProcessorResponse response = p.process(item);

      assertEquals(ProcessorResponse.ok(), response);

      // Check FileContent has been deleted
      assertEquals(0L, item.getContents(FileContent.class).count());

      // Check two tables have been created - one for each worksheet
      assertEquals(2L, item.getContents(TableContent.class).count());

      if (source.endsWith("xlsx")) {
        assertEquals(
            "EXCEL2007", item.getProperties().get(PropertyKeys.PROPERTY_KEY_VERSION).get());
      } else {
        assertEquals("EXCEL97", item.getProperties().get(PropertyKeys.PROPERTY_KEY_VERSION).get());
      }

      Map<String, String> sheetIds =
          item.getContents(TableContent.class)
              .collect(Collectors.toMap(WithDescription::getDescription, WithId::getId));

      TableContent tcStaff = (TableContent) item.getContent(sheetIds.get("Staff")).get();
      assertEquals(true, tcStaff.getProperties().get("active").get());
      assertEquals(true, tcStaff.getProperties().get("visible").get());

      Table staff = tcStaff.getData();

      assertEquals(5, staff.getColumnCount());
      assertTrue(
          staff
              .getColumnNames()
              .get()
              .containsAll(List.of("Name", "Age", "Favourite Colour", "Qualified", "")));

      assertEquals(5, staff.getRowCount());

      assertTrue(
          staff
              .getRows()
              .map(row -> row.getValueAt(0).get())
              .collect(Collectors.toList())
              .containsAll(List.of("Alice", "Bob", "Charlie", "Dave", "Eve")));
      assertTrue(
          staff
              .getRows()
              .map(row -> row.getValueAt(1).get())
              .collect(Collectors.toList())
              .containsAll(List.of(28.0, 43.0, 19.0, 24.0, 35.0)));
      assertTrue(
          staff
              .getRows()
              .map(row -> row.getValueAt(2).get())
              .collect(Collectors.toList())
              .containsAll(List.of("Red", "Orange", "Yellow", "Green", "Blue")));
      assertTrue(
          staff
              .getRows()
              .map(row -> row.getValueAt(3).orElse("NULL"))
              .collect(Collectors.toList())
              .containsAll(List.of(true, false, "NULL")));
      assertTrue(
          staff
              .getRows()
              .map(row -> row.getValueAt(4).orElse("NULL"))
              .collect(Collectors.toList())
              .containsAll(List.of("Qualification status currently unknown", "NULL")));

      TableContent tcDepartments =
          (TableContent) item.getContent(sheetIds.get("Departments")).get();
      assertEquals(false, tcDepartments.getProperties().get("active").get());
      assertEquals(true, tcDepartments.getProperties().get("visible").get());

      Table departments = tcDepartments.getData();

      assertEquals(2, departments.getColumnCount());
      assertTrue(
          departments.getColumnNames().get().containsAll(List.of("Department Title", "Site ID")));

      assertEquals(5, departments.getRowCount()); // Including one hidden row

      assertTrue(
          departments
              .getRows()
              .map(row -> row.getValueAt(0).get())
              .collect(Collectors.toList())
              .containsAll(
                  List.of("Finance", "HR", "Research", "Marketing", "Department of Mysteries")));
      assertTrue(
          departments
              .getRows()
              .map(row -> row.getValueAt(1).get())
              .collect(Collectors.toList())
              .containsAll(List.of(1.0, 2.0, 3.0, "Unknown")));
    }
  }
}
