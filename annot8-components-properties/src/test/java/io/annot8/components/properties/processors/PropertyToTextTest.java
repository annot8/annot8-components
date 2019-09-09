/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.annot8.components.monitor.resources.Logging;
import io.annot8.components.properties.processors.PropertyToText.PropertyToTextSettings;
import io.annot8.core.components.Processor;
import io.annot8.core.components.Resource;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.settings.EmptySettings;
import io.annot8.core.settings.Settings;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;

public class PropertyToTextTest {

  private static final String EXPECTED_KEY = "test";
  private static final String EXPECTED_VALUE = "Hello World!";

  @Test
  public void testNoSettings() throws Annot8Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(EXPECTED_KEY, EXPECTED_VALUE);

    Settings settings = EmptySettings.getInstance();

    doTest(properties, settings);
  }

  @Test
  public void testWhitelist() throws Annot8Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(EXPECTED_KEY, EXPECTED_VALUE);
    properties.put("foo", "bar");

    PropertyToTextSettings settings = new PropertyToTextSettings();
    settings.setWhitelist(new HashSet<>(Collections.singletonList(EXPECTED_KEY)));

    doTest(properties, settings);
  }

  @Test
  public void testBlacklist() throws Annot8Exception {
    Map<String, Object> properties = new HashMap<>();
    properties.put(EXPECTED_KEY, EXPECTED_VALUE);
    properties.put("foo", "bar");

    PropertyToTextSettings settings = new PropertyToTextSettings();
    settings.setBlacklist(new HashSet<>(Collections.singletonList("foo")));

    doTest(properties, settings);
  }

  private void doTest(Map<String, Object> properties, Settings settings) throws Annot8Exception {

    Logging logging = Logging.useLoggerFactory();
    Map<String, Resource> resources = new HashMap<>();
    resources.put("logging", logging);

    Context context = new TestContext(settings, resources);

    try (Processor p = new PropertyToText()) {

      p.configure(context);

      Item item = new TestItem();

      item.getProperties().set(properties);
      assertEquals(0, item.getContents().count());

      p.process(item);

      AtomicInteger count = new AtomicInteger();
      item.getContents()
          .forEach(
              c -> {
                count.getAndIncrement();
                assertEquals(EXPECTED_KEY, c.getName());
                assertEquals(EXPECTED_VALUE, c.getData());
              });

      assertEquals(1, count.get());
    }
  }
}
