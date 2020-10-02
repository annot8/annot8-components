/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class SimpleSpanElasticsearchSinkTest {
  @Test
  public void testItemToMap() {
    TestItem item = new TestItem();
    item.getProperties().set("test", 123);
    item.getProperties().set("test2", "Hello, World!");

    Map<String, Object> m = SimpleSpanElasticsearchSink.Processor.itemToMap(item);

    assertEquals(2, m.size());
    assertEquals("Item", m.get(SimpleSpanElasticsearchSink.Processor.TYPE_FIELD));
    assertNotNull(m.get(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD));

    Map<String, Object> p =
        (Map<String, Object>) m.get(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD);
    assertEquals(2, p.size());
    assertEquals(123, p.get("test"));
    assertEquals("Hello, World!", p.get("test2"));
  }

  @Test
  public void testEmptyItemToMap() {
    TestItem item = new TestItem();
    Map<String, Object> m = SimpleSpanElasticsearchSink.Processor.itemToMap(item);

    assertEquals(2, m.size());
    assertEquals("Item", m.get(SimpleSpanElasticsearchSink.Processor.TYPE_FIELD));
    assertTrue(m.containsKey(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD));

    Map<String, Object> p =
        (Map<String, Object>) m.get(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD);
    assertTrue(p.isEmpty());
  }

  @Test
  public void testTextToMap() {
    TestItem item = new TestItem();
    TestStringContent text =
        item.createContent(TestStringContent.class)
            .withData("Jack and Jill went up the hill")
            .withProperty("test", 123)
            .withProperty("test2", "Hello, World!")
            .save();

    text.getAnnotations().create().withType("Person").withBounds(new SpanBounds(0, 4)).save();
    text.getAnnotations().create().withType("Person").withBounds(new SpanBounds(9, 13)).save();

    Map<String, Object> m = SimpleSpanElasticsearchSink.Processor.textToMap(text, false);

    assertEquals(5, m.size());
    assertEquals("Text", m.get(SimpleSpanElasticsearchSink.Processor.TYPE_FIELD));
    assertEquals(item.getId(), m.get(SimpleSpanElasticsearchSink.Processor.PARENT_FIELD));
    assertNotNull(m.get(SimpleSpanElasticsearchSink.Processor.CONTENT_FIELD));
    assertTrue(m.containsKey(SimpleSpanElasticsearchSink.Processor.ANNOTATIONS_FIELD));
    assertTrue(m.containsKey(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD));

    Map<String, Object> a =
        (Map<String, Object>) m.get(SimpleSpanElasticsearchSink.Processor.ANNOTATIONS_FIELD);
    assertEquals(1, a.size());
    assertEquals(Set.of("Jack", "Jill"), a.get("Person"));

    Map<String, Object> p =
        (Map<String, Object>) m.get(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD);
    assertEquals(2, p.size());
    assertEquals(123, p.get("test"));
    assertEquals("Hello, World!", p.get("test2"));
  }

  @Test
  public void testTextToMapIgnoreCase() {
    TestItem item = new TestItem();
    TestStringContent text =
        item.createContent(TestStringContent.class)
            .withData("Jack and jAcK went up the hill")
            .withProperty("test", 123)
            .withProperty("test2", "Hello, World!")
            .save();

    text.getAnnotations().create().withType("Person").withBounds(new SpanBounds(0, 4)).save();
    text.getAnnotations().create().withType("Person").withBounds(new SpanBounds(9, 13)).save();

    Map<String, Object> m = SimpleSpanElasticsearchSink.Processor.textToMap(text, true);

    assertEquals(5, m.size());
    assertEquals("Text", m.get(SimpleSpanElasticsearchSink.Processor.TYPE_FIELD));
    assertEquals(item.getId(), m.get(SimpleSpanElasticsearchSink.Processor.PARENT_FIELD));
    assertNotNull(m.get(SimpleSpanElasticsearchSink.Processor.CONTENT_FIELD));
    assertTrue(m.containsKey(SimpleSpanElasticsearchSink.Processor.ANNOTATIONS_FIELD));
    assertTrue(m.containsKey(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD));

    Map<String, Object> a =
        (Map<String, Object>) m.get(SimpleSpanElasticsearchSink.Processor.ANNOTATIONS_FIELD);
    assertEquals(1, a.size());
    assertEquals(Set.of("JACK"), a.get("Person"));

    Map<String, Object> p =
        (Map<String, Object>) m.get(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD);
    assertEquals(2, p.size());
    assertEquals(123, p.get("test"));
    assertEquals("Hello, World!", p.get("test2"));
  }

  @Test
  public void testEmptyTextToMap() {
    TestItem item = new TestItem();
    Text text = new TestStringContent(item);

    Map<String, Object> m = SimpleSpanElasticsearchSink.Processor.textToMap(text, false);

    assertEquals(5, m.size());
    assertEquals("Text", m.get(SimpleSpanElasticsearchSink.Processor.TYPE_FIELD));
    assertEquals(item.getId(), m.get(SimpleSpanElasticsearchSink.Processor.PARENT_FIELD));
    assertNotNull(m.get(SimpleSpanElasticsearchSink.Processor.CONTENT_FIELD));
    assertTrue(m.containsKey(SimpleSpanElasticsearchSink.Processor.ANNOTATIONS_FIELD));
    assertTrue(m.containsKey(SimpleSpanElasticsearchSink.Processor.PROPERTIES_FIELD));
  }
}
