/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class SpacyServerSettingsTest {
  @Test
  public void test() {
    SpacyServerSettings s = new SpacyServerSettings();
    assertNotNull(s.getBaseUri());

    s.setBaseUri("https://example.com:1234/root/");
    assertEquals("https://example.com:1234/root/", s.getBaseUri());

    assertTrue(s.validate());

    s.setBaseUri("");
    assertFalse(s.validate());

    s.setBaseUri(null);
    assertFalse(s.validate());
  }
}
