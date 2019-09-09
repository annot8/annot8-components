/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.DefaultRow;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.components.db.processors.JDBCSettings;
import io.annot8.core.exceptions.Annot8RuntimeException;

public class DatabaseTableIterator implements Iterator<Row> {

  private ResultSet resultSet;
  private TableMetadata metadata;
  private JDBCSettings settings;
  private List<String> columnNames;

  public DatabaseTableIterator(ResultSet set, TableMetadata metadata) {
    this.resultSet = set;
    this.metadata = metadata;
    columnNames =
        this.metadata
            .getColumns()
            .stream()
            .map(ColumnMetadata::getName)
            .collect(Collectors.toList());
  }

  @Override
  public boolean hasNext() {
    if (resultSet == null) {
      return false;
    }
    try {
      if (resultSet.isClosed()) {
        return false;
      }
      return resultSet.getRow() != metadata.getRowCount();
    } catch (SQLException e) {
      throw new Annot8RuntimeException("Failed to fetch row count", e);
    }
  }

  @Override
  public Row next() {
    try {
      if (resultSet.isClosed()) {
        throw new NoSuchElementException("Result set is closed");
      }

      resultSet.next();
      Row row = resultSetToRow(resultSet);
      return row;
    } catch (SQLException e) {
      throw new Annot8RuntimeException("Failed to iterate results", e);
    }
  }

  private Row resultSetToRow(ResultSet resultSet) throws SQLException {
    int index = resultSet.getRow() - 1;
    List<Object> data = new ArrayList<>();
    for (int i = 1; i < metadata.getColumns().size() + 1; i++) {
      data.add(resultSet.getObject(i));
    }
    return new DefaultRow(index, columnNames, data);
  }
}
