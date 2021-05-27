/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SpacyTest {
  @Test
  public void testSettings() {
    Spacy.Settings s = new Spacy.Settings();

    assertTrue(s.isAddSentences());
    s.setAddSentences(false);
    assertFalse(s.isAddSentences());

    assertTrue(s.isAddTokens());
    s.setAddTokens(false);
    assertFalse(s.isAddTokens());

    assertTrue(s.isAddEntities());
    s.setAddEntities(false);
    assertFalse(s.isAddEntities());
  }
}
