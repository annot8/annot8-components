/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.annot8.components.gazetteers.processors.Gazetteer;

public class CollectionGazetteerTest {
  @Test
  public void test() {
    Gazetteer g = new CollectionGazetteer(Arrays.asList("hello", "ciao", "bonjour"));

    assertEquals(3, g.getValues().size());
    assertTrue(g.getValues().contains("ciao"));
    assertFalse(g.getValues().contains("gutentag"));

    assertArrayEquals(new String[] {"hello"}, g.getAliases("hello").toArray());

    assertEquals(Collections.emptyMap(), g.getAdditionalData("bonjour"));
  }
}
