/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import io.annot8.api.settings.Description;

public class DocumentExtractorSettings implements io.annot8.api.settings.Settings {
  private boolean extractMetadata = true;
  private boolean extractText = true;
  private boolean extractImages = true;
  private boolean extractTables = true;
  private boolean discardOriginal = false;

  @Override
  public boolean validate() {
    return extractMetadata | extractText | extractImages;
  }

  @Description(value = "Should metadata be extracted from the document", defaultValue = "true")
  public boolean isExtractMetadata() {
    return extractMetadata;
  }

  public void setExtractMetadata(boolean extractMetadata) {
    this.extractMetadata = extractMetadata;
  }

  @Description(value = "Should text be extracted from the document", defaultValue = "true")
  public boolean isExtractText() {
    return extractText;
  }

  public void setExtractText(boolean extractText) {
    this.extractText = extractText;
  }

  @Description(value = "Should images be extracted from the document", defaultValue = "true")
  public boolean isExtractImages() {
    return extractImages;
  }

  public void setExtractImages(boolean extractImages) {
    this.extractImages = extractImages;
  }

  @Description(value = "Should tables be extracted from the document", defaultValue = "true")
  public boolean isExtractTables() {
    return extractTables;
  }

  public void setExtractTables(boolean extractTables) {
    this.extractTables = extractTables;
  }

  @Description(
      value = "Discard original Content from which content is extracted",
      defaultValue = "false")
  public boolean isDiscardOriginal() {
    return discardOriginal;
  }

  public void setDiscardOriginal(boolean discardOriginal) {
    this.discardOriginal = discardOriginal;
  }
}
