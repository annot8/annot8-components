/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

/** Class for tests designed to be non serializable by Jackson */
public class NonSerializableTestData {

  private String nonAccessibleField;

  public NonSerializableTestData(String value) {
    this.nonAccessibleField = value;
  }
}
