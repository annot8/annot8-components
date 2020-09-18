/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.FileContent;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestFileContent;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationToPropertyTest {
  @Test
  public void testSettings() {
    AnnotationToProperty.Settings settings = new AnnotationToProperty.Settings();

    assertNotNull(settings.getStrategy());
    assertNotNull(settings.getPropertyName());
    assertNotNull(settings.getAnnotationType());

    assertTrue(settings.validate());

    settings.setStrategy(AnnotationToProperty.Strategy.LEAST_COMMON);
    assertEquals(AnnotationToProperty.Strategy.LEAST_COMMON, settings.getStrategy());

    settings.setAnnotationType("FooBarBaz");
    assertEquals("FooBarBaz", settings.getAnnotationType());

    settings.setPropertyName("BazBarFoo");
    assertEquals("BazBarFoo", settings.getPropertyName());

    assertTrue(settings.validate());

    settings.setStrategy(null);
    assertFalse(settings.validate());
    settings.setStrategy(AnnotationToProperty.Strategy.LEAST_COMMON);
    assertTrue(settings.validate());

    settings.setAnnotationType(null);
    assertFalse(settings.validate());
    settings.setAnnotationType("");
    assertFalse(settings.validate());
    settings.setAnnotationType("FooBarBaz");
    assertTrue(settings.validate());

    settings.setPropertyName(null);
    assertFalse(settings.validate());
    settings.setPropertyName("");
    assertFalse(settings.validate());
    settings.setPropertyName("BarBazFoo");
    assertTrue(settings.validate());
  }

  @Test
  public void testMostCommon() {
    testStrategy(AnnotationToProperty.Strategy.MOST_COMMON, "*", "A");
  }

  @Test
  public void testLeastCommon() {
    testStrategy(AnnotationToProperty.Strategy.LEAST_COMMON, "*", "ANNOTATE");
  }

  @Test
  public void testLeastCommonFiltered() {
    testStrategy(AnnotationToProperty.Strategy.LEAST_COMMON, "letter", "Z");
  }

  @Test
  public void testNoResults() {
    AnnotationToProperty.Settings s = new AnnotationToProperty.Settings();
    s.setAnnotationType("number");

    AnnotationToProperty.Processor p = new AnnotationToProperty.Processor(s);

    Item i = createTestItem();

    ProcessorResponse r = p.process(i);
    assertEquals(ProcessorResponse.ok(), r);

    assertTrue(i.getProperties().getAll().isEmpty());
  }

  @Test
  public void testMixed() {
    AnnotationToProperty.Settings s = new AnnotationToProperty.Settings();
    s.setAnnotationType("file");
    s.setPropertyName("test");

    AnnotationToProperty.Processor p = new AnnotationToProperty.Processor(s);

    Item i = createTestItem();

    FileContent fc =
        i.createContent(TestFileContent.class)
            .withData(new File("testMixed.tmp"))
            .withDescription("File")
            .save();

    fc.getAnnotations().create().withBounds(ContentBounds.getInstance()).withType("file").save();

    ProcessorResponse r = p.process(i);
    assertEquals(ProcessorResponse.ok(), r);

    Map<String, Object> m = i.getProperties().getAll();
    assertEquals(1, m.size());
    assertTrue(m.containsKey("test"));
    assertNotNull(m.get("test"));
  }

  private void testStrategy(AnnotationToProperty.Strategy strategy, String type, Object expected) {
    AnnotationToProperty.Settings s = new AnnotationToProperty.Settings();
    s.setStrategy(strategy);
    s.setAnnotationType(type);
    s.setPropertyName(strategy.name());

    AnnotationToProperty.Processor p = new AnnotationToProperty.Processor(s);

    Item i = createTestItem();

    ProcessorResponse r = p.process(i);
    assertEquals(ProcessorResponse.ok(), r);

    Map<String, Object> m = i.getProperties().getAll();
    assertEquals(1, m.size());
    assertTrue(m.containsKey(strategy.name()));
    assertEquals(expected, m.get(strategy.name()));
  }

  private Item createTestItem() {
    Item item = new TestItem();

    item.createContent(TestStringContent.class)
        .withData("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        .withDescription("Alphabet")
        .save();

    item.createContent(TestStringContent.class)
        .withData("ZYXWVUTSRQPONMLKJIHGFEDCBA")
        .withDescription("Alphabet backwards")
        .save();

    item.createContent(TestStringContent.class)
        .withData("ABCDEFGHIJKLMNOPQRSTUVWXY")
        .withDescription("Alphabet no Z")
        .save();

    item.createContent(TestStringContent.class).withData("AEIOU").withDescription("Vowels").save();

    TestStringContent w =
        item.createContent(TestStringContent.class)
            .withData("ANNOTATE")
            .withDescription("Word")
            .save();

    w.getAnnotations().create().withBounds(new SpanBounds(0, 8)).withType("word").save();

    item.getContents(TestStringContent.class)
        .forEach(
            c -> {
              for (int i = 0; i < c.getData().length(); i++)
                c.getAnnotations()
                    .create()
                    .withBounds(new SpanBounds(i, i + 1))
                    .withType("letter")
                    .save();
            });

    return item;
  }
}
