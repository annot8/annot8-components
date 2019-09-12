/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo;

import io.annot8.common.components.AbstractComponent;
import io.annot8.components.mongo.resources.Mongo;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.components.mongo.resources.MongoConnectionSettings;

/**
 * Base class for Mongo components which simplifies configuration
 *
 * <p>Implement configure(context, connection) to set up using the best connection.
 */
// @SettingsClass(value = MongoSettings.class, optional = true)
// @SettingsClass(value = MongoConnectionSettings.class, optional = true)
// @UsesResource(value = Mongo.class, optional = true)
public abstract class AbstractMongoComponent extends AbstractComponent {

  private MongoConnection connection = null;
  private MongoConnectionSettings settings;

  protected AbstractMongoComponent(MongoConnectionSettings settings) {
    this.settings = settings;
  }

  public AbstractMongoComponent(MongoConnection connection) {
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
