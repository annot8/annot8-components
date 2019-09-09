/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.Row;
import io.annot8.components.files.AbstractCSVDataTest;

public class CSVTableTest extends AbstractCSVDataTest {

  @Test
  public void testCSVTable() {
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
    assertEquals("test", row.getValueAt(0).get());
    assertEquals("test", row.getValueAt(1).get());
    assertEquals("test", row.getValueAt(1).get());
    Row row2 = rows.get(1);
    assertEquals("test2", row2.getValueAt(0).get());
    assertEquals("test2", row2.getValueAt(1).get());
    assertEquals("test2", row2.getValueAt(1).get());
    Row row3 = rows.get(2);
    assertEquals("test3", row3.getValueAt(0).get());
    assertEquals("test3", row3.getValueAt(1).get());
    assertEquals("test3", row3.getValueAt(1).get());
  }
}
