/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ExtractionWithPropertiesTest {

  @Test
  public void testValueAndMap() {
    Map<String, Object> m = new HashMap<>();
    m.put("name", "James");

    ExtractionWithProperties<String> ewp = new ExtractionWithProperties<>("foo", m);
    assertEquals("foo", ewp.getExtractedValue());
    assertEquals(m, ewp.getProperties());
  }

  @Test
  public void testValueOnly() {
    ExtractionWithProperties<String> ewp = new ExtractionWithProperties<>("foo");
    assertEquals("foo", ewp.getExtractedValue());
    assertEquals(Collections.emptyMap(), ewp.getProperties());
  }
}
