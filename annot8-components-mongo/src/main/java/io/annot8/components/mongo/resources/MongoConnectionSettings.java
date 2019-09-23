/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.resources;

import com.google.common.base.Strings;
import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;

import java.util.Optional;
import java.util.stream.Stream;

public class MongoConnectionSettings implements Settings {

  private String connection;
  private String database;
  private String collection;

  @Description("Connection string")
  public String getConnection() {
    return connection;
  }

  public void setConnection(String connection) {
    this.connection = connection;
  }

  @Description("Database name")
  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  @Description("Collection name")
  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  @Override
  public boolean validate() {
    return validateConnection()
        && database != null
        && !database.isEmpty()
        && collection != null
        && !collection.isEmpty();
  }

  public boolean validateConnection() {
    return !Strings.isNullOrEmpty(connection)
        && (connection.startsWith("mongodb://") || connection.startsWith("mongodb+srv://"));
  }

  public MongoConnectionSettings merge(Optional<MongoConnectionSettings> settings) {
    if (settings.isPresent()) {

      MongoConnectionSettings s = settings.get();

      if (!validateConnection()) {
        this.connection = s.getConnection();
      }

      if (Strings.isNullOrEmpty(database)) {
        this.database = s.getDatabase();
      }

      if (Strings.isNullOrEmpty(collection)) {
        this.collection = s.getCollection();
      }
    }

    return this;
  }

  public static MongoConnectionSettings merge(Optional<MongoConnectionSettings>... settings) {

    MongoConnectionSettings s = new MongoConnectionSettings();

    Stream.of(settings).forEach(s::merge);

    return s;
  }
}
