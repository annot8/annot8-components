/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.resources;

public class DefaultMongoConnectionSettings extends MongoConnectionSettings {

  public DefaultMongoConnectionSettings() {
    this.setConnection("mongodb://localhost");
    this.setDatabase("annot8");
  }
}
