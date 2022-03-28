/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo;

import io.annot8.common.components.AbstractComponent;
import io.annot8.components.mongo.resources.MongoConnectionSettings;
import io.annot8.components.mongo.utils.Mongo;
import io.annot8.components.mongo.utils.MongoConnection;

/**
 * Base class for Mongo components which simplifies configuration
 *
 * <p>Implement configure(context, connection) to set up using the best connection.
 */
public abstract class AbstractMongoComponent extends AbstractComponent {

  private MongoConnection connection = null;
  private MongoConnectionSettings settings;

  protected AbstractMongoComponent(MongoConnectionSettings settings) {
    this.settings = settings;
  }

  protected AbstractMongoComponent(MongoConnection connection) {
    this.connection = connection;
  }

  protected MongoConnection getConnection() {
    if (connection == null) {
      connection = new Mongo(settings);
    }
    return connection;
  }

  @Override
  public void close() {
    if (connection != null) {
      connection.disconnect();
    }
  }
}
