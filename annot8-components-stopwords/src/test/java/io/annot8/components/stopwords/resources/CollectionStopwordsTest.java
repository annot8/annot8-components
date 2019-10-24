/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.resources;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class CollectionStopwordsTest {
  @Test
  public void testDefault() {
    Stopwords sw = new CollectionStopwords("en", Arrays.asList("and", "the"));

    assertEquals("en", sw.getLanguage());
    assertTrue(sw.isStopword("and"));
    assertTrue(sw.isStopword(" THE "));
    assertFalse(sw.isStopword("foo"));
  }
}
