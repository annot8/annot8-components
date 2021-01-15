/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.data;

import java.util.Collections;
import java.util.Map;

public class ExtractionWithProperties<T> {
  private final T extractedValue;
  private final Map<String, Object> properties;

  public ExtractionWithProperties(T extractedValue) {
    this.extractedValue = extractedValue;
    this.properties = Collections.emptyMap();
  }

  public ExtractionWithProperties(T extractedValue, Map<String, Object> properties) {
    this.extractedValue = extractedValue;
    this.properties = properties;
  }

  public T getExtractedValue() {
    return extractedValue;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}
