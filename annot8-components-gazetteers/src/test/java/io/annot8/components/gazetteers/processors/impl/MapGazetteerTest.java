/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.annot8.components.gazetteers.processors.Gazetteer;

public class MapGazetteerTest {
  @Test
  public void test() {
    Set<String> skodaTerms = Set.of("Fabia", "Superb", "Octavia");
    Map<String, Object> skodaData = new HashMap<>();
    skodaData.put("manufacturer", "Skoda");
    skodaData.put("nationality", "Czech");

    Set<String> renaultTerms = Set.of("Clio", "Scenic", "Captur", "Twingo");
    Map<String, Object> renaultData = new HashMap<>();
    renaultData.put("manufacturer", "Renault");
    renaultData.put("nationality", "French");

    Map<Set<String>, Map<String, Object>> terms = new HashMap<>();
    terms.put(skodaTerms, skodaData);
    terms.put(renaultTerms, renaultData);

    Gazetteer g = new MapGazetteer(terms);

    assertEquals(7, g.getValues().size());
    assertTrue(g.getValues().contains("Superb"));
    assertFalse(g.getValues().contains("Focus"));

    assertArrayEquals(
        Arrays.stream(new String[] {"Fabia", "Superb", "Octavia"}).sorted().toArray(),
        g.getAliases("Octavia").stream().sorted().toArray());

    assertEquals(renaultData, g.getAdditionalData("Scenic"));
  }
}
