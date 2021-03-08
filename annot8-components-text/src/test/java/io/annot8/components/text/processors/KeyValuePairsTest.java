/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class KeyValuePairsTest {
  @Test
  public void test() {
    Item item = new TestItem();
    Text text =
        item.createContent(Text.class)
            .withData(
                "Name: Alice\nAge : 21 \r\nFavourite Food: Pizza, Chips  \nHair Colour: Light Brown\nSocial media: Twitter: @alice\nEmpty key:  ")
            .save();

    text.getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .withBounds(new SpanBounds(6, 11))
        .withProperty(PropertyKeys.PROPERTY_KEY_GENDER, "female")
        .save();

    text.getAnnotations()
        .create()
        .withType("capitalLetter")
        .withBounds(new SpanBounds(6, 7))
        .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, "A")
        .save();

    text.getAnnotations()
        .create()
        .withType("food")
        .withBounds(new SpanBounds(39, 44))
        .withProperty("calories", 300)
        .save();
    text.getAnnotations()
        .create()
        .withType("food")
        .withBounds(new SpanBounds(46, 61))
        .withProperty("calories", 500)
        .save();

    text.getAnnotations()
        .create()
        .withType("colour")
        .withBounds(new SpanBounds(73, 78))
        .withProperty("hex", "#964B00")
        .save();

    KeyValuePairs kvp = new KeyValuePairs();
    kvp.setSettings(new KeyValuePairs.Settings());

    assertNotNull(kvp.capabilities());

    Processor p = kvp.create(new SimpleContext());

    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(
        5, text.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_METADATA).count());

    List<Annotation> annotations =
        text.getAnnotations()
            .getByType(AnnotationTypes.ANNOTATION_TYPE_METADATA)
            .sorted(
                Comparator.comparingInt(
                    a -> a.getBounds(SpanBounds.class).orElse(new SpanBounds(0, 0)).getBegin()))
            .collect(Collectors.toList());

    assertEquals(
        "Name", annotations.get(0).getProperties().get(PropertyKeys.PROPERTY_KEY_KEY).get());
    assertEquals(
        "Alice", annotations.get(0).getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "female",
        annotations
            .get(0)
            .getProperties()
            .get("entity", Map.class)
            .get()
            .get(PropertyKeys.PROPERTY_KEY_GENDER));
    assertEquals(
        AnnotationTypes.ANNOTATION_TYPE_PERSON,
        annotations.get(0).getProperties().get(PropertyKeys.PROPERTY_KEY_TYPE).get());
    assertFalse(annotations.get(0).getProperties().get("capitalLetter").isPresent());
    assertEquals(0, annotations.get(0).getBounds(SpanBounds.class).get().getBegin());
    assertEquals(11, annotations.get(0).getBounds(SpanBounds.class).get().getEnd());

    assertEquals(
        "Age", annotations.get(1).getProperties().get(PropertyKeys.PROPERTY_KEY_KEY).get());
    assertEquals(
        "21", annotations.get(1).getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(12, annotations.get(1).getBounds(SpanBounds.class).get().getBegin());
    assertEquals(20, annotations.get(1).getBounds(SpanBounds.class).get().getEnd());

    assertEquals(
        "Favourite Food",
        annotations.get(2).getProperties().get(PropertyKeys.PROPERTY_KEY_KEY).get());
    assertEquals(
        List.of("Pizza", "Chips"),
        annotations.get(2).getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertFalse(annotations.get(2).getProperties().get("entity").isPresent());
    assertEquals(23, annotations.get(2).getBounds(SpanBounds.class).get().getBegin());
    assertEquals(51, annotations.get(2).getBounds(SpanBounds.class).get().getEnd());

    assertEquals(
        "Hair Colour", annotations.get(3).getProperties().get(PropertyKeys.PROPERTY_KEY_KEY).get());
    assertEquals(
        "Light Brown",
        annotations.get(3).getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "#964B00", annotations.get(3).getProperties().get("entity", Map.class).get().get("hex"));

    assertEquals(
        "colour", annotations.get(3).getProperties().get(PropertyKeys.PROPERTY_KEY_TYPE).get());
    assertEquals(54, annotations.get(3).getBounds(SpanBounds.class).get().getBegin());
    assertEquals(78, annotations.get(3).getBounds(SpanBounds.class).get().getEnd());

    assertEquals(
        "Social media",
        annotations.get(4).getProperties().get(PropertyKeys.PROPERTY_KEY_KEY).get());
    assertEquals(
        "Twitter: @alice",
        annotations.get(4).getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
  }
}
