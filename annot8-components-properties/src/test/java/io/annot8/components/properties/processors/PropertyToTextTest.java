/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import io.annot8.components.properties.processors.PropertyToText.PropertyToTextSettings;
import io.annot8.core.components.Processor;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyToTextTest {

  private static final String KEY = "Text from property from test";
  private static final String EXPECTED_DESC = "Text from property from " + KEY;

  private static final String EXPECTED_VALUE = "Hello World!";

  @Test
  public void testWhitelist() throws Annot8Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(KEY, EXPECTED_VALUE);
    properties.put("foo", "bar");

    PropertyToTextSettings settings = new PropertyToTextSettings();
    settings.setWhitelist(new HashSet<>(Collections.singletonList(KEY)));

    doTest(properties, settings);
  }

  @Test
  public void testBlacklist() throws Annot8Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(KEY, EXPECTED_VALUE);
    properties.put("foo", "bar");

    PropertyToTextSettings settings = new PropertyToTextSettings();
    settings.setBlacklist(new HashSet<>(Collections.singletonList("foo")));

    doTest(properties, settings);
  }

  private void doTest(Map<String, Object> properties, PropertyToTextSettings settings) {



    try (Processor p = new PropertyToText(settings)) {

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
