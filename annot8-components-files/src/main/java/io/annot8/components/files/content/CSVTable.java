/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.content;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.components.files.utils.BufferedReaderIterator;
import io.annot8.core.exceptions.Annot8RuntimeException;

public class CSVTable implements Table {

  public static final String PATTERN = ",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
  private final File file;
  private boolean hasHeaders;
  private TableMetadata metadata;

  public CSVTable(File file, boolean hasHeaders) {
    this.file = file;
    this.hasHeaders = hasHeaders;
    this.metadata = init();
  }

  @Override
  public int getColumnCount() {
    return metadata.getColumns().size();
  }

  @Override
  public int getRowCount() {
    return metadata.getRowCount();
  }

  @Override
  public Optional<List<String>> getColumnNames() {
    List<String> names =
        metadata.getColumns().stream().map(ColumnMetadata::getName).collect(Collectors.toList());
    return Optional.ofNullable(names);
  }

  @Override
  public Stream<Row> getRows() {
    final FileReader fileReader;
    final BufferedReader bufferedReader;
    try {
      fileReader = new FileReader(file);
      bufferedReader = new BufferedReader(fileReader);
    } catch (FileNotFoundException e) {
      throw new Annot8RuntimeException("Failed to read file ", e);
    }
    BufferedReaderIterator iterator =
        new BufferedReaderIterator(bufferedReader, metadata, hasHeaders);
    Iterable<Row> iterable = () -> iterator;
    Stream<Row> stream = StreamSupport.stream(iterable.spliterator(), false);
    stream.onClose(
        () -> {
          try {
            bufferedReader.close();
            fileReader.close();
          } catch (IOException e) {
            throw new Annot8RuntimeException("Failed to close resources", e);
          }
        });
    return stream;
  }

  private TableMetadata init() {
    List<String> columnNames = null;
    int rowCount = 0;
    int columnCount = -1;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line = null;
      if (hasHeaders) {
        String[] data = reader.readLine().split(PATTERN);
        columnNames = Arrays.asList(data);
      }
      while ((line = reader.readLine()) != null) {
        rowCount++;
        if (columnCount == -1) {
          String[] data = line.split(PATTERN);
          columnCount = data.length;
        } else if (columnCount != line.split(PATTERN).length) {
          throw new Annot8RuntimeException(
              "CSV file an irregular number of columns at line " + rowCount);
        }
      }
    } catch (IOException e) {
      throw new Annot8RuntimeException("Failed to read csv file", e);
    }

    if (!hasHeaders) {
      columnNames = new ArrayList<>(columnCount);
    }
    return new TableMetadata(file.getName(), "CSV", toColumnMetadata(columnNames), rowCount);
  }

  private List<ColumnMetadata> toColumnMetadata(List<String> columnNames) {
    return columnNames.stream().map(s -> new ColumnMetadata(s, 0)).collect(Collectors.toList());
  }
}
