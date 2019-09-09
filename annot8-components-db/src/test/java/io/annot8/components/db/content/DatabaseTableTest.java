/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.components.db.processors.AbstractSQLiteDataTest;
import io.annot8.components.db.processors.JDBCSettings;
import io.annot8.core.exceptions.Annot8RuntimeException;

public class DatabaseTableTest extends AbstractSQLiteDataTest {

  @Test
  public void testDatabaseTable() {
    DatabaseTable table = new DatabaseTable(getTestDBMetadata(), getTestDBSettings());

    assertEquals(3, table.getColumnCount());
    assertEquals(2, table.getRowCount());
    assertTrue(table.getColumnNames().isPresent());
    assertThat(table.getColumnNames().get()).containsExactly("test", "id", "someValue");

    Stream<Row> rowsStream = table.getRows();
    List<Row> rows = rowsStream.collect(Collectors.toList());
    rowsStream.close();
    Row row1 = rows.get(0);
    Row row2 = rows.get(1);
    assertEquals("test", row1.getValueAt(0).get());
    assertEquals(1, (int) row1.getInt(1).get());
    assertEquals("value", row1.getValueAt(2).get());
    assertEquals("test2", row2.getValueAt(0).get());
    assertEquals(2, (int) row2.getInt(1).get());
    assertEquals("value2", row2.getValueAt(2).get());
  }

  @Test
  public void testDatabaseTableBadSettings() {
    JDBCSettings settings = new JDBCSettings("");
    DatabaseTable table =
        new DatabaseTable(new TableMetadata("test", "TABLE", Collections.emptyList(), 0), settings);

    assertThrows(Annot8RuntimeException.class, () -> table.getRows());
  }
}
