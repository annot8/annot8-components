/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DocumentExtractorSettingsTest {
  @Test
  public void test() {
    DocumentExtractorSettings settings = new DocumentExtractorSettings();

    assertTrue(settings.isExtractMetadata());
    assertTrue(settings.isExtractText());
    assertTrue(settings.isExtractImages());
    assertTrue(settings.validate());

    settings.setExtractMetadata(false);
    assertFalse(settings.isExtractMetadata());
    assertTrue(settings.validate());

    settings.setExtractText(false);
    assertFalse(settings.isExtractText());
    assertTrue(settings.validate());

    settings.setExtractImages(false);
    assertFalse(settings.isExtractImages());
    assertFalse(settings.validate()); // Everything now false, so shouldn't be valid
  }
}
