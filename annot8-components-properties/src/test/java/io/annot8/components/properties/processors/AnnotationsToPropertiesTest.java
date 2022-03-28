/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AnnotationsToPropertiesTest {
  @Test
  public void testMostNone() {
    Item item = createTestItem();

    AnnotationsToProperties.Settings s = new AnnotationsToProperties.Settings();
    s.setAnnotationType(List.of(AnnotationTypes.ANNOTATION_TYPE_PERSON));
    s.setPropertyNameKey("key");
    s.setPropertyValueKey("value");
    s.setStrategy(AnnotationsToProperties.Strategy.MOST_COMMON);
    s.setPropertyNameTransformation(AnnotationsToProperties.StringTransformation.NONE);

    try (AnnotationsToProperties.Processor p = new AnnotationsToProperties.Processor(s)) {
      p.process(item);

      assertEquals(1, item.getProperties().getAll().size());
      assertTrue(item.getProperties().has("First Name"));
      assertEquals("Alice", item.getProperties().get("First Name").get());
    }
  }

  @Test
  public void testLeastCamel() {
    Item item = createTestItem();

    AnnotationsToProperties.Settings s = new AnnotationsToProperties.Settings();
    s.setAnnotationType(List.of(AnnotationTypes.ANNOTATION_TYPE_PERSON));
    s.setPropertyNameKey("key");
    s.setPropertyValueKey("value");
    s.setStrategy(AnnotationsToProperties.Strategy.LEAST_COMMON);
    s.setPropertyNameTransformation(AnnotationsToProperties.StringTransformation.CAMEL_CASE);

    try (AnnotationsToProperties.Processor p = new AnnotationsToProperties.Processor(s)) {
      p.process(item);

      assertEquals(1, item.getProperties().getAll().size());
      assertTrue(item.getProperties().has("firstName"));
      assertEquals("Al", item.getProperties().get("firstName").get());
    }
  }

  private Item createTestItem() {
    Item item = new TestItem();

    Text t1 = item.createContent(Text.class).withData("Name: Al").save();

    t1.getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .withBounds(new SpanBounds(0, 8))
        .withProperty("key", "First Name")
        .withProperty("value", "Al")
        .save();

    Text t2 = item.createContent(Text.class).withData("Name: Alice").save();

    t2.getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .withBounds(new SpanBounds(0, 11))
        .withProperty("key", "First Name")
        .withProperty("value", "Alice")
        .save();

    Text t3 = item.createContent(Text.class).withData("Name: Alice").save();

    t3.getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .withBounds(new SpanBounds(0, 11))
        .withProperty("key", "First Name")
        .withProperty("value", "Alice")
        .save();

    return item;
  }
}
