/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.core.settings.Settings;

public class CSVExtractorSettings implements Settings {

  private final boolean hasHeaders;

  public CSVExtractorSettings(boolean hasHeaders) {
    this.hasHeaders = hasHeaders;
  }

  @Override
  public boolean validate() {
    return true;
  }

  public boolean hasHeaders() {
    return hasHeaders;
  }
}
