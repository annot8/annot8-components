/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.resources;

import java.util.Optional;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.annot8.components.base.components.AbstractResource;
import io.annot8.core.context.Context;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;

public class Mongo extends AbstractResource implements MongoConnection {

  private MongoClient client;
  private MongoDatabase database;
  private MongoCollection<Document> collection;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    Optional<MongoFactory> mongoFactory = context.getResource(MongoFactory.class);
    if (!mongoFactory.isPresent()) {
      throw new MissingResourceException("MongoFactory is required");
    }

    Optional<MongoConnectionSettings> mongoSettings =
        context.getSettings(MongoConnectionSettings.class);
    if (!mongoSettings.isPresent()) {
      throw new BadConfigurationException("MongoConnectionSettings are required");
    }

    MongoConnectionSettings mergedSettings =
        mongoFactory.get().mergeWithDefaultSettings(mongoSettings);

    if (!mergedSettings.validate()) {
      throw new BadConfigurationException("MongoConnectionSettings are incomplete");
    }

    client = mongoFactory.get().buildClient(mergedSettings);
    database = client.getDatabase(mergedSettings.getDatabase());
    collection = database.getCollection(mergedSettings.getCollection());
  }

  public MongoDatabase getDatabase() {
    return database;
  }

  public MongoCollection getCollection() {
    return collection;
  }

  @Override
  public void disconnect() {
    // Do nothing - managed by Resource.close
  }

  @Override
  public void close() {
    if (client != null) {
      client.close();
    }
  }
}
