/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.annot8.api.components.Processor;
import io.annot8.api.properties.ImmutableProperties;
import io.annot8.api.settings.NoSettings;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class PostcodeTest {

  @Test
  public void testPorton() {
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem
            .createContent(TestStringContent.class)
            .withData("Porton Down is located at SP4 0JQ.")
            .save();

    Postcode postcode = new Postcode();
    Processor postcodeProcessor = postcode.createComponent(null, NoSettings.getInstance());
    postcodeProcessor.process(testItem);

    ImmutableProperties result =
        content.getAnnotations().getAll().findAny().orElseThrow().getProperties();
    assertEquals("SP40JQ", result.getOrDefault("postcode", ""));
    assertEquals(-1.6988, result.getOrDefault(PropertyKeys.PROPERTY_KEY_LONGITUDE, 0.0), 0.001);
    assertEquals(51.1346, result.getOrDefault(PropertyKeys.PROPERTY_KEY_LATITUDE, 0.0), 0.001);
  }

  @Test
  public void testWrongPorton() {
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem
            .createContent(TestStringContent.class)
            .withData("Porton Down is not located at JP4 0JQ.")
            .save();

    Postcode postcode = new Postcode();
    Processor postcodeProcessor = postcode.createComponent(null, NoSettings.getInstance());
    postcodeProcessor.process(testItem);

    ImmutableProperties result =
        content.getAnnotations().getAll().findAny().orElseThrow().getProperties();
    assertFalse(result.has("postcode"));
    assertFalse(result.has(PropertyKeys.PROPERTY_KEY_LONGITUDE));
    assertFalse(result.has(PropertyKeys.PROPERTY_KEY_LATITUDE));
  }

  @Test
  public void testNonPatterns() {
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem
            .createContent(TestStringContent.class)
            .withData("Q000AB\n WR15 0CI\nX12 0EB")
            .save();

    Postcode postcode = new Postcode();
    Processor postcodeProcessor = postcode.createComponent(null, NoSettings.getInstance());
    postcodeProcessor.process(testItem);

    assertEquals(0L, content.getAnnotations().getAll().count());
  }

  @Test
  public void testAll() throws IOException {
    try (InputStream resource = PostcodeTest.class.getResourceAsStream("ukpostcodes.csv"); ) {
      String doc =
          new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
              .lines()
              .collect(Collectors.joining("\\n"));

      TestItem testItem = new TestItem();
      TestStringContent content =
          testItem.createContent(TestStringContent.class).withData(doc).save();

      Postcode postcode = new Postcode();
      Processor postcodeProcessor = postcode.createComponent(null, NoSettings.getInstance());
      postcodeProcessor.process(testItem);

      assertEquals(1717777L, content.getAnnotations().getAll().count());
    }
  }
}
