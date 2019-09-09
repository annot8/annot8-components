/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.TableMetadata;

public class DatabaseTableIteratorTest {

  private void runTestInConnection(Consumer<ResultSet> testRunner) {
    String url = "jdbc:sqlite:/" + getTestFile().getAbsolutePath();
    String query = "SELECT * FROM " + getTableMetadata().getName();
    try (Connection connection = DriverManager.getConnection(url);
        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery(query)) {
      testRunner.accept(set);
    } catch (SQLException e) {
      fail("Do not expect errors connecting to the test data", e);
    }
  }

  @Test
  public void testDatabaseTableIterator() {
    runTestInConnection(
        (set) -> {
          DatabaseTableIterator iterator = new DatabaseTableIterator(set, getTableMetadata());
          assertTrue(iterator.hasNext());
          assertNotNull(iterator.next());
          assertNotNull(iterator.next());
          assertFalse(iterator.hasNext());
        });

    runTestInConnection(
        (set) -> {
          DatabaseTableIterator iterator1 = new DatabaseTableIterator(set, getTableMetadata());
          Iterable<Row> iterable = () -> iterator1;
          Stream<Row> stream = StreamSupport.stream(iterable.spliterator(), false);
          stream.forEach(
              r -> {
                assertNotNull(r.getValueAt(0).get());
                assertNotNull(r.getValueAt(1).get());
                assertNotNull(r.getValueAt(2).get());
              });
        });
  }

  private TableMetadata getTableMetadata() {
    List<ColumnMetadata> columnMetadata = new ArrayList<>();
    columnMetadata.add(new ColumnMetadata("test", 2000000l));
    columnMetadata.add(new ColumnMetadata("id", 2000000l));
    columnMetadata.add(new ColumnMetadata("someValue", 2000000l));
    return new TableMetadata("test", "TABLE", columnMetadata, 2);
  }

  private File getTestFile() {
    URL resource = DatabaseTableIteratorTest.class.getClassLoader().getResource("test.db");
    File file = null;
    try {
      return new File(resource.toURI());
    } catch (URISyntaxException e) {
      fail("Error not expected when finding test file");
    }
    return null;
  }
}
