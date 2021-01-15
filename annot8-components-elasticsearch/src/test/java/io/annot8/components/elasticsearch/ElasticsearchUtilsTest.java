/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ElasticsearchUtilsTest {
  @Test
  public void testPersistData() {
    assertTrue(ElasticsearchUtils.shouldPersistData(String.class));
    assertTrue(ElasticsearchUtils.shouldPersistData(Integer.class));
    assertTrue(ElasticsearchUtils.shouldPersistData(Long.class));
    assertTrue(ElasticsearchUtils.shouldPersistData(Double.class));
    assertTrue(ElasticsearchUtils.shouldPersistData(Boolean.class));

    assertFalse(ElasticsearchUtils.shouldPersistData(InputStream.class));
    assertFalse(ElasticsearchUtils.shouldPersistData(Map.class));
  }

  @Test
  public void testItemToMap() {
    TestItem item1 = new TestItem();
    item1.getProperties().set("foo", "bar");
    item1.getProperties().set("baz", 123);

    TestItem item2 = new TestItem(item1.getId());

    Map<String, Object> m1 = ElasticsearchUtils.itemToMap(item1, false);
    assertEquals(2, m1.size());
    assertFalse(m1.containsKey(ElasticsearchUtils.PARENT));
    assertEquals(item1.getId(), m1.get(ElasticsearchUtils.ID));

    Map<String, Object> props = (Map<String, Object>) m1.get(ElasticsearchUtils.PROPERTIES);
    assertEquals(2, props.size());
    assertEquals("bar", props.get("foo"));
    assertEquals(123, props.get("baz"));

    Map<String, Object> m2 = ElasticsearchUtils.itemToMap(item2, false);
    assertEquals(2, m2.size());

    assertEquals(item2.getId(), m2.get(ElasticsearchUtils.ID));
    assertEquals(item1.getId(), m2.get(ElasticsearchUtils.PARENT));
  }

  @Test
  public void testGroupToMap() {
    TestItem item = new TestItem();
    TestStringContent tsc =
        item.createContent(TestStringContent.class)
            .withData("Rosie and Jim met by the canal.")
            .save();

    Annotation rosie =
        tsc.getAnnotations()
            .create()
            .withType("entity/person")
            .withBounds(new SpanBounds(0, 5))
            .save();

    Annotation jim =
        tsc.getAnnotations()
            .create()
            .withType("entity/person")
            .withBounds(new SpanBounds(10, 13))
            .save();

    Annotation canal =
        tsc.getAnnotations()
            .create()
            .withType("entity/location")
            .withBounds(new SpanBounds(25, 30))
            .save();

    Group g =
        item.getGroups()
            .create()
            .withType("event")
            .withProperty("probability", 0.9)
            .withAnnotation("actor", rosie)
            .withAnnotation("actor", jim)
            .withAnnotation("location", canal)
            .save();

    Map<String, Object> m = ElasticsearchUtils.groupToMap(g, false);
    assertEquals(4, m.size());
    assertEquals(g.getId(), m.get(ElasticsearchUtils.ID));
    assertEquals("event", m.get(ElasticsearchUtils.TYPE));

    Map<String, Object> props = (Map<String, Object>) m.get(ElasticsearchUtils.PROPERTIES);
    assertEquals(1, props.size());
    assertEquals(0.9, props.get("probability"));

    Map<String, Object> roles = (Map<String, Object>) m.get(ElasticsearchUtils.ROLES);
    assertEquals(2, roles.size());

    List<Map<String, Object>> actors = (List<Map<String, Object>>) roles.get("actor");
    assertEquals(2, actors.size());
    actors.forEach(a -> assertEquals(2, a.size()));
    actors.forEach(a -> assertEquals(tsc.getId(), a.get(ElasticsearchUtils.CONTENT_ID)));
    assertTrue(
        actors.stream()
            .map(a -> a.get(ElasticsearchUtils.ANNOTATION_ID))
            .collect(Collectors.toList())
            .containsAll(List.of(rosie.getId(), jim.getId())));

    List<Map<String, Object>> locations = (List<Map<String, Object>>) roles.get("location");
    assertEquals(1, locations.size());

    Map<String, Object> l = locations.get(0);
    assertEquals(2, l.size());
    assertEquals(tsc.getId(), l.get(ElasticsearchUtils.CONTENT_ID));
    assertEquals(canal.getId(), l.get(ElasticsearchUtils.ANNOTATION_ID));
  }

  @Test
  public void testContentToMap() {
    TestItem item = new TestItem();
    TestStringContent tsc =
        item.createContent(TestStringContent.class)
            .withData("Hello, World!")
            .withDescription("Test content")
            .withProperty("test", "abc")
            .save();

    Map<String, Object> m = ElasticsearchUtils.contentToMap(tsc, false);
    assertEquals(5, m.size());
    assertNotNull(m.get(ElasticsearchUtils.ID));
    assertEquals("java.lang.String", m.get(ElasticsearchUtils.CONTENT_TYPE));
    assertEquals("Hello, World!", m.get(ElasticsearchUtils.CONTENT));
    assertEquals("Test content", m.get(ElasticsearchUtils.DESCRIPTION));

    Map<String, Object> props = (Map<String, Object>) m.get(ElasticsearchUtils.PROPERTIES);
    assertEquals(1, props.size());
    assertEquals("abc", props.get("test"));
  }

  @Test
  public void testAnnotationToMap() {
    TestItem item = new TestItem();
    TestStringContent tsc =
        item.createContent(TestStringContent.class)
            .withData("Hello, World!")
            .withDescription("Test content")
            .withProperty("test", "abc")
            .save();

    Annotation a1 =
        tsc.getAnnotations()
            .create()
            .withType("entity/location")
            .withBounds(new SpanBounds(7, 12))
            .withProperty("size", "planet")
            .save();

    Annotation a2 =
        tsc.getAnnotations()
            .create()
            .withType("grammar/sentence")
            .withBounds(ContentBounds.getInstance())
            .save();

    Map<String, Object> m1 = ElasticsearchUtils.annotationToMap(a1, tsc, false);
    assertEquals(7, m1.size());
    assertNotNull(m1.get(ElasticsearchUtils.ID));
    assertEquals("entity/location", m1.get(ElasticsearchUtils.TYPE));
    assertEquals("io.annot8.common.data.bounds.SpanBounds", m1.get(ElasticsearchUtils.BOUNDS_TYPE));

    assertEquals(7, m1.get(ElasticsearchUtils.BEGIN));
    assertEquals(12, m1.get(ElasticsearchUtils.END));
    assertEquals("World", m1.get(ElasticsearchUtils.VALUE));

    Map<String, Object> props = (Map<String, Object>) m1.get(ElasticsearchUtils.PROPERTIES);
    assertEquals(1, props.size());
    assertEquals("planet", props.get("size"));

    Map<String, Object> m2 = ElasticsearchUtils.annotationToMap(a2, tsc, false);
    assertEquals(3, m2.size());
    assertNotNull(m2.get(ElasticsearchUtils.ID));
    assertEquals("grammar/sentence", m2.get(ElasticsearchUtils.TYPE));
    assertEquals(
        "io.annot8.common.data.bounds.ContentBounds", m2.get(ElasticsearchUtils.BOUNDS_TYPE));
  }

  @Test
  public void testToStringMap() {
    Map<String, Object> m1 = new HashMap<>();
    m1.put("a", "abc");
    m1.put("b", 123);
    m1.put("c", true);

    Map<String, Object> m = new HashMap<>();
    m.put("a", "abc");
    m.put("b", 123);
    m.put("c", true);
    m.put("d", m1);

    Map<String, Object> e1 = new HashMap<>();
    e1.put("a", "abc");
    e1.put("b", "123");
    e1.put("c", "true");

    Map<String, Object> e = new HashMap<>();
    e.put("a", "abc");
    e.put("b", "123");
    e.put("c", "true");
    e.put("d", e1);

    assertEquals(e, ElasticsearchUtils.toStringMap(m));
  }
}
