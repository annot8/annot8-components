/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.resources;

import io.annot8.core.settings.Settings;

public class MongoSettings implements Settings {
  private String mongo = null;

  public String getMongo() {
    return mongo;
  }

  public void getMongo(String mongo) {
    this.mongo = mongo;
  }

  public void setMongo(String mongo) {
    this.mongo = mongo;
  }

  public boolean hasMongo() {
    return mongo != null && !mongo.isEmpty();
  }

  @Override
  public boolean validate() {
    return true;
  }
}
