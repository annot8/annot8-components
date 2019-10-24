/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.exceptions.UnsupportedContentException;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.components.db.content.DatabaseTable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extracts database tables from an SQLite file
 *
 * <p>Note that whilst this class uses JdbcSettings internally, it does not accept it as user
 * definable configuration.
 */
@ComponentName("SQLite Database Table Extractor")
@ComponentDescription("Extracts database tables from SQLite files")
public class SQLiteDatabaseTableExtractor
    extends AbstractProcessorDescriptor<SQLiteDatabaseTableExtractor.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(FileContent.class)
        .withCreatesContent(TableContent.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {

    // TODO: Ought to add these to conventions, or use an existing one
    public static final String PROPERTY_NAME = "TABLE_NAME";
    public static final String PROPERTY_TYPE = "TABLE_METADATA";

    private static final String SCHEMA_QUERY = "pragma schema_version";
    private static final String TABLE_SIZE_PREFIX = "SELECT count(*) FROM ";

    @Override
    public ProcessorResponse process(Item item) {
      boolean withoutError =
          item.getContents(FileContent.class)
              .filter(this::isSQLite)
              .map(c -> createTables(item, c))
              .reduce(true, (a, b) -> a && b);

      return withoutError ? ProcessorResponse.ok() : ProcessorResponse.itemError();
    }

    private boolean createTables(Item item, FileContent sqliteFile) {
      JdbcSettings settings = getConnectionSettings(sqliteFile);
      List<TableMetadata> tables = getTables(settings);

      return tables.stream()
          .map(t -> createDatabaseTable(item, t, settings))
          .reduce(true, (a, b) -> a && b);
    }

    private boolean createDatabaseTable(
        Item item, TableMetadata tableMetadata, JdbcSettings settings) {
      try {
        item.createContent(TableContent.class)
            .withDescription(String.format("Extracted from SQL table[%s]", tableMetadata.getName()))
            .withData(new DatabaseTable(tableMetadata, settings))
            .withProperty(PROPERTY_NAME, tableMetadata.getName())
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

      JdbcSettings settings = getConnectionSettings(content);
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

    private JdbcSettings getConnectionSettings(FileContent content) {
      String jdbcUrl = "jdbc:sqlite:/" + content.getData().getAbsolutePath();
      return new JdbcSettings(jdbcUrl);
    }

    private List<TableMetadata> getTables(JdbcSettings settings) {
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
}
