/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.resources;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.exceptions.BadConfigurationException;
import org.junit.jupiter.api.Test;

public class StopwordsIsoTest {

  @Test
  public void testDefault() {
    try (Stopwords sw = new StopwordsIso()) {

      assertEquals("en", sw.getLanguage());
      assertTrue(sw.isStopword("and"));
    }
  }

  @Test
  public void testEnglish() {
    try (Stopwords sw = new StopwordsIso("en")) {

      assertEquals("en", sw.getLanguage());
      assertTrue(sw.isStopword("and"));
      assertTrue(sw.isStopword("YOUR"));
      assertTrue(sw.isStopword("Why"));
      assertFalse(sw.isStopword("java"));
      assertFalse(sw.isStopword("JAVA"));
      assertFalse(sw.isStopword("Java"));
    }
  }

  @Test
  public void testMissing() {
    assertThrows(BadConfigurationException.class, () -> new StopwordsIso("missing"));
  }
}
