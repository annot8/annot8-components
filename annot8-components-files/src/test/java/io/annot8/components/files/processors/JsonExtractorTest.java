/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.TestItemFactory;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class JsonExtractorTest {

  @Test
  public void testSettings() {
    JsonExtractor.Settings s = new JsonExtractor.Settings();

    assertTrue(s.validate());

    assertNotNull(s.getJsonExtension());
    s.setJsonExtension(List.of("abc"));
    assertEquals(List.of("abc"), s.getJsonExtension());

    assertNotNull(s.getJsonlExtension());
    s.setJsonlExtension(List.of("xyz"));
    assertEquals(List.of("xyz"), s.getJsonlExtension());

    assertNull(s.getContentFields());
    s.setContentFields(List.of("content"));
    assertEquals(List.of("content"), s.getContentFields());
  }

  @Test
  public void testCreate() {
    JsonExtractor je = new JsonExtractor();
    assertNotNull(je.capabilities());

    JsonExtractor.Processor p = je.createComponent(null, new JsonExtractor.Settings());
    assertNotNull(p);
    p.close();
  }

  @Test
  public void testJson() throws Exception {
    JsonExtractor.Settings settings = new JsonExtractor.Settings();
    settings.setContentFields(List.of("stringValue1", "stringValue2"));

    try (Processor p = new JsonExtractor.Processor(settings)) {
      TestItem item = new TestItem();

      URL resource = JsonExtractorTest.class.getResource("test.json");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class).withDescription("test.json").withData(f).save();

      p.process(item);

      // Test properties
      Map<String, Object> mProps = item.getProperties().getAll();
      assertEquals(9, mProps.size());

      assertFalse(mProps.containsKey("nullValue"));
      assertEquals("Howdy y'all", mProps.get("stringValue3"));
      assertEquals(42L, mProps.get("intValue"));
      assertEquals(new BigDecimal("1.234"), mProps.get("doubleValue"));
      assertEquals(true, mProps.get("trueValue"));
      assertEquals(false, mProps.get("falseValue"));
      assertEquals(List.of("A", "B", "C"), mProps.get("arrayOfStrings"));
      assertEquals(List.of(1L, 2L, 3L), mProps.get("arrayOfNumbers"));
      assertEquals(List.of("A", 1L), mProps.get("mixedArray"));

      Map<String, Object> mObject = new HashMap<>();
      mObject.put("stringValue", "Hello");
      mObject.put("intValue", 100L);

      assertEquals(mObject, mProps.get("object"));

      // Test content
      assertEquals(2, item.getContent().size());
      AtomicInteger textContentFound = new AtomicInteger();
      item.getContents(Text.class)
          .forEach(
              t -> {
                assertTrue(t.getProperties().has(PropertyKeys.PROPERTY_KEY_IDENTIFIER));
                assertTrue(t.getProperties().has(PropertyKeys.PROPERTY_KEY_SOURCE));
                assertTrue(t.getProperties().has(PropertyKeys.PROPERTY_KEY_ACCESSEDAT));

                String key =
                    t.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_IDENTIFIER, String.class)
                        .orElse("ERROR");
                if (key.equals("stringValue1")) {
                  assertEquals("Hello, world!", t.getData());
                  textContentFound.addAndGet(1);
                } else if (key.equals("stringValue2")) {
                  assertEquals("Bonjour le monde!", t.getData());
                  textContentFound.addAndGet(10);
                } else {
                  fail("Unexpected content found: " + key);
                }
              });

      assertEquals(11, textContentFound.get());
    }
  }

  @Test
  public void testJsonNonText() throws Exception {
    JsonExtractor.Settings settings = new JsonExtractor.Settings();
    settings.setContentFields(List.of("intValue", "nullValue"));

    try (Processor p = new JsonExtractor.Processor(settings)) {
      TestItem item = new TestItem();

      URL resource = JsonExtractorTest.class.getResource("test.json");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class).withDescription("test.json").withData(f).save();

      p.process(item);

      // Test content
      assertEquals(1, item.getContent().size());
      item.getContents(Text.class)
          .forEach(
              t -> {
                assertTrue(t.getProperties().has(PropertyKeys.PROPERTY_KEY_IDENTIFIER));
                assertTrue(t.getProperties().has(PropertyKeys.PROPERTY_KEY_SOURCE));
                assertTrue(t.getProperties().has(PropertyKeys.PROPERTY_KEY_ACCESSEDAT));

                assertEquals("42", t.getData());
              });
    }
  }

  @Test
  public void testJsonAllContent() throws Exception {
    JsonExtractor.Settings settings = new JsonExtractor.Settings();
    settings.setContentFields(null);

    try (Processor p = new JsonExtractor.Processor(settings)) {
      TestItem item = new TestItem();

      URL resource = JsonExtractorTest.class.getResource("test.json");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class).withDescription("test.json").withData(f).save();

      p.process(item);

      // Test properties
      Map<String, Object> mProps = item.getProperties().getAll();
      assertEquals(0, mProps.size());

      // Test content
      assertEquals(11, item.getContent().size());
    }
  }

  @Test
  public void testBadJson() throws Exception {
    JsonExtractor.Settings settings = new JsonExtractor.Settings();
    settings.setJsonExtension(List.of("txt"));

    try (Processor p = new JsonExtractor.Processor(settings)) {
      TestItem item = new TestItem();

      URL resource = JsonExtractorTest.class.getResource("testfilemetadata.txt");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class)
          .withDescription("testfilemetadata.txt")
          .withData(f)
          .save();

      p.process(item);

      // Test content
      assertEquals(1, item.getContent().size());
      assertEquals(1L, item.getContents(FileContent.class).count());
    }
  }

  @Test
  public void testJsonl() throws Exception {
    JsonExtractor.Settings settings = new JsonExtractor.Settings();
    settings.setContentFields(List.of("content"));

    try (Processor p = new JsonExtractor.Processor(settings)) {
      TestItem item = new TestItem();
      TestItemFactory itemFactory = (TestItemFactory) item.getItemFactory();

      URL resource = JsonExtractorTest.class.getResource("test.jsonl");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class).withDescription("test.jsonl").withData(f).save();

      p.process(item);

      // Test no content in original item
      assertEquals(0, item.getContent().size());

      // Test created items (i.e. each line)
      List<Item> children = itemFactory.getCreatedItems();
      assertEquals(2, children.size());

      children.forEach(
          i -> {
            assertEquals(1L, i.getContents().count());

            Text t = i.getContents(Text.class).findFirst().orElseThrow();

            assertTrue(i.getProperties().has("line"));
            long line = i.getProperties().get("line", Long.class).orElse(-1L);
            if (line == 1L) {
              assertEquals("Line 1", t.getData());
            } else if (line == 2L) {
              assertEquals("Line 2", t.getData());
            } else {
              fail("Unexpected property value");
            }
          });
    }
  }

  @Test
  public void testBadJsonl() throws Exception {
    JsonExtractor.Settings settings = new JsonExtractor.Settings();
    settings.setJsonlExtension(List.of("txt"));

    try (Processor p = new JsonExtractor.Processor(settings)) {
      TestItem item = new TestItem();

      URL resource = JsonExtractorTest.class.getResource("testfilemetadata.txt");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class)
          .withDescription("testfilemetadata.txt")
          .withData(f)
          .save();

      p.process(item);

      // Test content
      assertEquals(1, item.getContent().size());
      assertEquals(1L, item.getContents(FileContent.class).count());
    }
  }
}
