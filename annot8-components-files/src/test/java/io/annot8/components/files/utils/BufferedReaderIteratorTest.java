/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.components.files.AbstractCSVDataTest;

public class BufferedReaderIteratorTest extends AbstractCSVDataTest {

  private void testWithResources(Consumer<BufferedReader> consumer) {
    try (FileReader reader = new FileReader(getTestData("test.csv"));
        BufferedReader bufferedReader = new BufferedReader(reader)) {
      consumer.accept(bufferedReader);
    } catch (IOException e) {
      fail("Failed to find resources", e);
    }
  }

  @Test
  public void testBufferedReaderIterator() {
    testWithResources(
        (reader) -> {
          BufferedReaderIterator iterator =
              new BufferedReaderIterator(reader, getTestMetadata(), true);
          assertTrue(iterator.hasNext());
          Row row1 = iterator.next();
          assertTrue(iterator.hasNext());
          Row row2 = iterator.next();
          assertTrue(iterator.hasNext());
          Row row3 = iterator.next();
          assertFalse(iterator.hasNext());

          assertEquals("test", row1.getValueAt(0).get());
          assertEquals("test", row1.getValueAt(1).get());
          assertEquals("test", row1.getValueAt(2).get());
          assertEquals("test2", row2.getValueAt(0).get());
          assertEquals("test2", row2.getValueAt(1).get());
          assertEquals("test2", row2.getValueAt(2).get());
          assertEquals("test3", row3.getValueAt(0).get());
          assertEquals("test3", row3.getValueAt(1).get());
          assertEquals("test3", row3.getValueAt(2).get());
        });
  }

  private TableMetadata getTestMetadata() {
    ColumnMetadata metadata = new ColumnMetadata("firstCol", 0);
    ColumnMetadata metadata2 = new ColumnMetadata("secondCol", 0);
    ColumnMetadata metadata3 = new ColumnMetadata("thirdCol", 0);
    return new TableMetadata("test.csv", "CSV", Arrays.asList(metadata, metadata2, metadata3), 3);
  }
}
