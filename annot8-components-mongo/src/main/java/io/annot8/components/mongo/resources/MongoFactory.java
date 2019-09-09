/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.resources;

import java.util.Optional;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.components.Resource;
import io.annot8.core.context.Context;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.SettingsClass;

/** Factory for creating Mongo connections from {@link MongoConnectionSettings} */
@SettingsClass(value = MongoConnectionSettings.class, optional = true)
public class MongoFactory extends AbstractComponent implements Resource {

  private Optional<MongoConnectionSettings> defaultSettings = Optional.empty();

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    defaultSettings = context.getSettings(MongoConnectionSettings.class);
  }

  public Optional<MongoConnection> buildMongo(Optional<MongoConnectionSettings> settings) {
    try {
      MongoConnectionSettings mergedSettings = mergeWithDefaultSettings(settings);

      if (!mergedSettings.validate()) {
        throw new BadConfigurationException("MongoConnectionSettings are incomplete");
      }

      MongoClient client = buildClient(mergedSettings);
      MongoDatabase database = client.getDatabase(mergedSettings.getDatabase());
      MongoCollection<Document> collection = database.getCollection(mergedSettings.getCollection());

      return Optional.of(new SimpleMongoConnection(client, database, collection));
    } catch (BadConfigurationException e) {
      return Optional.empty();
    }
  }

  public MongoClient buildClient(MongoConnectionSettings settings)
      throws BadConfigurationException {

    if (settings == null || !settings.validateConnection()) {
      throw new BadConfigurationException("Connection settings are not valid");
    }

    return MongoClients.create(settings.getConnection());
  }

  public MongoConnectionSettings mergeWithDefaultSettings(
      Optional<MongoConnectionSettings> settings) {
    return MongoConnectionSettings.merge(settings, defaultSettings);
  }
}
