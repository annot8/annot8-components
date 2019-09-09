/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.processors;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.components.db.content.DatabaseTable;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.core.exceptions.UnsupportedContentException;

@ProcessesContent(FileContent.class)
@CreatesContent(TableContent.class)
public class SQLiteDatabaseTableExtractor extends AbstractComponent implements Processor {

  public static final String PROPERTY_TYPE = "TABLE_METADATA";
  private static final String SCHEMA_QUERY = "pragma schema_version";
  private static final String TABLE_SIZE_PREFIX = "SELECT count(*) FROM ";

  @Override
  public ProcessorResponse process(Item item) throws Annot8Exception {
    boolean withoutError =
        item.getContents(FileContent.class)
            .filter(this::isSQLite)
            .map(c -> createTables(item, c))
            .reduce(true, (a, b) -> a && b);

    return withoutError ? ProcessorResponse.ok() : ProcessorResponse.itemError();
  }

  private boolean createTables(Item item, FileContent sqliteFile) {
    JDBCSettings settings = getConnectionSettings(sqliteFile);
    List<TableMetadata> tables = getTables(settings);

    return tables
        .stream()
        .map(t -> createDatabaseTable(item, t, settings))
        .reduce(true, (a, b) -> a && b);
  }

  private boolean createDatabaseTable(
      Item item, TableMetadata tableMetadata, JDBCSettings settings) {
    try {
      item.create(TableContent.class)
          .withName(tableMetadata.getName())
          .withData(new DatabaseTable(tableMetadata, settings))
          .withProperty(PROPERTY_TYPE, tableMetadata)
          .save();
      return true;
    } catch (UnsupportedContentException e) {
      log().error("Failed to produce content of the given type ", e);
      return false;
    } catch (IncompleteException e) {
      log().error("Failed to save content", e);
      return false;
    }
  }

  private boolean isSQLite(FileContent content) {
    if (!content.getData().exists()) {
      return false;
    }

    JDBCSettings settings = getConnectionSettings(content);
    try (Connection connection = DriverManager.getConnection(settings.getJdbcUrl())) {
      ResultSet set = connection.createStatement().executeQuery(SCHEMA_QUERY);
      int schemaVersion = 0;
      while (set.next()) {
        schemaVersion = set.getInt("schema_version");
        break;
      }
      if (schemaVersion > 0) {
        return true;
      }
    } catch (SQLException e) {
      // Indicates that this file is not SQLite DB
      return false;
    }

    return false;
  }

  private JDBCSettings getConnectionSettings(FileContent content) {
    String jdbcUrl = "jdbc:sqlite:/" + content.getData().getAbsolutePath();
    return new JDBCSettings(jdbcUrl);
  }

  private List<TableMetadata> getTables(JDBCSettings settings) {
    List<TableMetadata> tables = new ArrayList<>();
    try (Connection connection = DriverManager.getConnection(settings.getJdbcUrl())) {
      DatabaseMetaData metaData = connection.getMetaData();
      ResultSet set = metaData.getTables(null, null, null, null);
      while (set.next()) {
        String tableName = set.getString("TABLE_NAME");
        String tableType = set.getString("TABLE_TYPE");
        List<ColumnMetadata> columns = getColumnMetadata(metaData, tableName);
        int rowSize = getRowCount(connection, tableName);
        tables.add(new TableMetadata(tableName, tableType, columns, rowSize));
      }
    } catch (SQLException e) {
      log().error("Failed to extract table names", e);
      return Collections.emptyList();
    }
    return tables;
  }

  private List<ColumnMetadata> getColumnMetadata(DatabaseMetaData metadata, String tableName)
      throws SQLException {
    List<ColumnMetadata> columnMetadata = new ArrayList<>();
    ResultSet set = metadata.getColumns(null, null, tableName, null);
    while (set.next()) {
      String columnName = set.getString("COLUMN_NAME");
      String type = set.getString("TYPE_NAME");
      long size = set.getLong("COLUMN_SIZE");
      columnMetadata.add(new ColumnMetadata(columnName, size));
    }
    return columnMetadata;
  }

  private int getRowCount(Connection connection, String tableName) {
    try (Statement statement = connection.createStatement()) {
      ResultSet resultSet = statement.executeQuery(TABLE_SIZE_PREFIX + tableName);
      return resultSet.getInt(1);
    } catch (SQLException e) {
      log().error("Failed to fetch table size for " + tableName, e);
      return -1;
    }
  }
}
