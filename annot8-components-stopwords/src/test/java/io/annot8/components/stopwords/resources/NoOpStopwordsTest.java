/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class NoOpStopwordsTest {
  @Test
  public void test() {
    try (Stopwords sw = new NoOpStopwords()) {

      assertEquals("*", sw.getLanguage());
      assertFalse(sw.isStopword("and"));
    }
  }
}
