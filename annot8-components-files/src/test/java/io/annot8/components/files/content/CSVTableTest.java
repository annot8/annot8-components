/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.common.data.content.Row;
import io.annot8.components.files.AbstractCSVDataTest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class CSVTableTest extends AbstractCSVDataTest {

  @Test
  public void testCSVTableHeaders() {
    CSVTable table = new CSVTable(getTestData("test.csv"), true);

    assertEquals(3, table.getColumnCount());
    assertEquals(3, table.getRowCount());
    assertTrue(table.getColumnNames().isPresent());
    assertThat(table.getColumnNames().get()).containsExactly("firstCol", "secondCol", "thirdCol");

    Stream<Row> rowsStream = table.getRows();
    List<Row> rows = rowsStream.collect(Collectors.toList());
    rowsStream.close();

    assertEquals(3, rows.size());
    Row row = rows.get(0);
    assertEquals(0, row.getRowIndex());
    assertEquals("test", row.getValueAt(0).get());
    assertEquals("test", row.getValueAt(1).get());
    assertEquals("test", row.getValueAt(2).get());
    Row row2 = rows.get(1);
    assertEquals(1, row2.getRowIndex());
    assertEquals("test2", row2.getValueAt(0).get());
    assertEquals("test2", row2.getValueAt(1).get());
    assertEquals("test2", row2.getValueAt(2).get());
    Row row3 = rows.get(2);
    assertEquals(2, row3.getRowIndex());
    assertEquals("test3", row3.getValueAt(0).get());
    assertEquals("test3", row3.getValueAt(1).get());
    assertEquals("test3", row3.getValueAt(2).get());
  }

  @Test
  public void testCSVTableNoHeaders() {
    CSVTable table = new CSVTable(getTestData("test-noheader.csv"), false);

    assertEquals(3, table.getColumnCount());
    assertEquals(3, table.getRowCount());
    assertTrue(table.getColumnNames().isPresent());
    assertThat(table.getColumnNames().get()).containsExactly("Column 1", "Column 2", "Column 3");

    Stream<Row> rowsStream = table.getRows();
    List<Row> rows = rowsStream.collect(Collectors.toList());
    rowsStream.close();

    assertEquals(3, rows.size());
    Row row = rows.get(0);
    assertEquals(0, row.getRowIndex());
    assertEquals("test", row.getValueAt(0).get());
    assertEquals("test", row.getValueAt(1).get());
    assertEquals("test", row.getValueAt(2).get());
    Row row2 = rows.get(1);
    assertEquals(1, row2.getRowIndex());
    assertEquals("test2", row2.getValueAt(0).get());
    assertEquals("test2", row2.getValueAt(1).get());
    assertEquals("test2", row2.getValueAt(2).get());
    Row row3 = rows.get(2);
    assertEquals(2, row3.getRowIndex());
    assertEquals("test3", row3.getValueAt(0).get());
    assertEquals("test3", row3.getValueAt(1).get());
    assertEquals("test3", row3.getValueAt(2).get());
  }

  @Test
  public void testCSVTableTricky() {
    CSVTable table = new CSVTable(getTestData("test-tricky.csv"), true);

    assertEquals(4, table.getColumnCount());
    assertEquals(6, table.getRowCount());
    assertTrue(table.getColumnNames().isPresent());
    assertThat(table.getColumnNames().get()).containsExactly("col1", "col1", "col3", "Column 4");

    Stream<Row> rowsStream = table.getRows();
    List<Row> rows = rowsStream.collect(Collectors.toList());
    rowsStream.close();

    assertEquals(6, rows.size());
    Row row1 = rows.get(0);
    assertEquals(0, row1.getRowIndex());
    assertEquals(3, row1.getColumnCount());
    assertEquals("test1", row1.getValueAt(0).get());
    assertEquals("test1", row1.getValueAt(1).get());
    assertEquals("test1", row1.getValueAt(2).get());

    Row row2 = rows.get(1);
    assertEquals(1, row2.getRowIndex());
    assertEquals(4, row2.getColumnCount());
    assertEquals("test2", row2.getValueAt(0).get());
    assertEquals("test2", row2.getValueAt(1).get());
    assertEquals("test2", row2.getValueAt(2).get());
    assertEquals("test2", row2.getValueAt(3).get());

    Row row3 = rows.get(2);
    assertEquals(2, row3.getRowIndex());
    assertEquals(3, row3.getColumnCount());
    assertEquals("test3", row3.getValueAt(0).get());
    assertEquals("test3", row3.getValueAt(1).get());
    assertEquals("", row3.getValueAt(2).get());

    Row row4 = rows.get(3);
    assertEquals(3, row4.getRowIndex());
    assertEquals(3, row4.getColumnCount());
    assertEquals("test4", row4.getValueAt(0).get());
    assertEquals("", row4.getValueAt(1).get());
    assertEquals("test4", row4.getValueAt(2).get());

    Row row5 = rows.get(4);
    assertEquals(4, row5.getRowIndex());
    assertEquals(2, row5.getColumnCount());
    assertEquals("test5", row5.getValueAt(0).get());
    assertEquals("test5", row5.getValueAt(1).get());

    Row row6 = rows.get(5);
    assertEquals(5, row6.getRowIndex());
    assertEquals(3, row6.getColumnCount());
    assertEquals("test6", row6.getValueAt(0).get());
    assertEquals("test6", row6.getValueAt(1).get());
    assertEquals("test6", row6.getValueAt(2).get());
  }
}
