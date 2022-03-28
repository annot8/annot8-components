/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.data;

import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Simple Table implemenation */
public class SimpleTable implements Table {
  private final List<String> columnNames;
  private final List<Row> rows;

  public SimpleTable(List<String> columnNames, List<Row> rows) {
    this.columnNames = Collections.unmodifiableList(columnNames);
    this.rows = Collections.unmodifiableList(rows);
  }

  @Override
  public int getColumnCount() {
    if (columnNames.isEmpty()) {
      return rows.stream().mapToInt(Row::getColumnCount).max().orElse(0);
    } else {
      return columnNames.size();
    }
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  @Override
  public Optional<List<String>> getColumnNames() {
    if (columnNames.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(columnNames);
    }
  }

  @Override
  public Stream<Row> getRows() {
    return rows.stream();
  }
}
