/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.testing.testimpl.TestItem;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class PropertyToTextTest {

  private static final String KEY = "Text from property from test";
  private static final String EXPECTED_DESC = "Text from property from " + KEY;

  private static final String EXPECTED_VALUE = "Hello World!";

  @Test
  public void testWhitelist() throws Annot8Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(KEY, EXPECTED_VALUE);
    properties.put("foo", "bar");

    PropertyToText.Settings settings = new PropertyToText.Settings();
    settings.setWhitelist(new HashSet<>(Collections.singletonList(KEY)));

    doTest(properties, settings);
  }

  @Test
  public void testBlacklist() throws Annot8Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(KEY, EXPECTED_VALUE);
    properties.put("foo", "bar");

    PropertyToText.Settings settings = new PropertyToText.Settings();
    settings.setBlacklist(new HashSet<>(Collections.singletonList("foo")));

    doTest(properties, settings);
  }

  private void doTest(Map<String, Object> properties, PropertyToText.Settings settings) {

    try (Processor p =
        new PropertyToText.Processor(settings.getWhitelist(), settings.getBlacklist())) {

      Item item = new TestItem();

      item.getProperties().set(properties);
      assertEquals(0, item.getContents().count());

      p.process(item);

      AtomicInteger count = new AtomicInteger();
      item.getContents()
          .forEach(
              c -> {
                count.getAndIncrement();
                assertEquals(EXPECTED_DESC, c.getDescription());
                assertEquals(EXPECTED_VALUE, c.getData());
              });

      assertEquals(1, count.get());
    }
  }
}
