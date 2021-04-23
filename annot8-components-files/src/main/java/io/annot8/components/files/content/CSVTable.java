/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.content;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.DefaultRow;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableMetadata;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CSVTable implements Table {
  private final TableMetadata metadata;
  private final List<Row> rows = new ArrayList<>();

  public CSVTable(File file, boolean hasHeaders) {
    CsvReader reader;
    try {
      reader = CsvReader.builder().build(file.toPath(), Charset.defaultCharset());
    } catch (IOException e) {
      throw new ProcessingException("Unable to read CSV file", e);
    }

    int index = (hasHeaders ? -1 : 0);

    int maxCols = 0;
    List<String> headers = Collections.emptyList();

    for (CsvRow csvRow : reader) {
      if (index < 0) {
        headers = csvRow.getFields();
      } else {
        rows.add(
            new DefaultRow(
                index,
                getHeaders(headers, csvRow.getFieldCount()),
                new ArrayList<>(csvRow.getFields())));
      }

      maxCols = Math.max(maxCols, csvRow.getFieldCount());
      index++;
    }

    metadata =
        new TableMetadata(
            file.getName(), "CSV", toColumnMetadata(getHeaders(headers, maxCols)), index);
  }

  public static List<String> getHeaders(List<String> knownHeaders, int colCount) {
    if (colCount == knownHeaders.size()) {
      return knownHeaders;
    } else if (colCount < knownHeaders.size()) {
      return knownHeaders.subList(0, colCount);
    } else {
      return Stream.concat(
              knownHeaders.stream(),
              IntStream.range(knownHeaders.size(), colCount).mapToObj(i -> "Column " + (i + 1)))
          .collect(Collectors.toList());
    }
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
    return rows.stream();
  }

  private List<ColumnMetadata> toColumnMetadata(Collection<String> columnNames) {
    return columnNames.stream().map(s -> new ColumnMetadata(s, 0)).collect(Collectors.toList());
  }
}
