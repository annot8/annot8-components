/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.content;

import io.annot8.api.exceptions.Annot8RuntimeException;
import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.components.db.processors.JdbcSettings;
import io.annot8.components.db.utils.DatabaseTableIterator;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DatabaseTable implements Table {

  private JdbcSettings settings;
  private TableMetadata tableMetadata;

  public DatabaseTable(TableMetadata metadata, JdbcSettings settings) {
    this.tableMetadata = metadata;
    this.settings = settings;
  }

  @Override
  public int getColumnCount() {
    return tableMetadata.getColumns().size();
  }

  @Override
  public int getRowCount() {
    return tableMetadata.getRowCount();
  }

  @Override
  // Closed by stream onClose below
  @SuppressWarnings("java:S2095")
  public Stream<Row> getRows() {
    final Connection connection;
    final Statement statement;
    final ResultSet resultSet;

    try {
      connection = getConnection();
      statement = connection.createStatement();
      resultSet = statement.executeQuery("SELECT * FROM " + tableMetadata.getName());
    } catch (SQLException e) {
      throw new Annot8RuntimeException("Failed to read database", e);
    }

    DatabaseTableIterator iterator = new DatabaseTableIterator(resultSet, tableMetadata);
    Iterable<Row> iterable = () -> iterator;
    Stream<Row> stream = StreamSupport.stream(iterable.spliterator(), false);
    stream.onClose(
        () -> {
          try {
            resultSet.close();
            statement.close();
            connection.close();
          } catch (SQLException e) {
            throw new Annot8RuntimeException("Failed to close resources", e);
          }
        });
    return stream;
  }

  @Override
  public Optional<List<String>> getColumnNames() {
    List<String> names =
        tableMetadata.getColumns().stream()
            .map(ColumnMetadata::getName)
            .collect(Collectors.toList());

    return Optional.of(names);
  }

  private Connection getConnection() throws SQLException {
    if (settings.getUser() != null && !settings.getUser().isEmpty()) {
      return DriverManager.getConnection(
          settings.getJdbcUrl(), settings.getUser(), settings.getPassword());
    } else {
      return DriverManager.getConnection(settings.getJdbcUrl());
    }
  }
}
