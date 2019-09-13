/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.DefaultRow;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.components.files.content.CSVTable;
import io.annot8.api.exceptions.Annot8RuntimeException;

public class BufferedReaderIterator implements Iterator<Row> {

  private TableMetadata metadata;
  private boolean hasHeaders;
  private BufferedReader reader;
  private int currentRow = 0;
  private List<String> columns;

  public BufferedReaderIterator(BufferedReader reader, TableMetadata metadata, boolean hasHeaders) {
    this.reader = reader;
    this.metadata = metadata;
    this.hasHeaders = hasHeaders;
    this.columns =
        metadata.getColumns().stream().map(ColumnMetadata::getName).collect(Collectors.toList());
  }

  @Override
  public boolean hasNext() {
    return currentRow < metadata.getRowCount();
  }

  @Override
  public Row next() {
    try {
      if (currentRow == 0 && hasHeaders) {
        // Move past the header row
        reader.readLine();
      }
      String line = reader.readLine();
      currentRow++;
      return toRow(line);
    } catch (IOException e) {
      throw new Annot8RuntimeException("Failed to read next line", e);
    }
  }

  private Row toRow(String line) {
    String[] data = line.split(CSVTable.PATTERN);
    return new DefaultRow(currentRow, columns, Arrays.asList(data));
  }
}
