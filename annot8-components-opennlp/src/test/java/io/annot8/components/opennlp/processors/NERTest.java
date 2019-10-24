/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NERTest {
  @Test
  public void test() {
    Item item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class)
            .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Jane Doe.")
            .save();

    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .withBounds(new SpanBounds(0, 36))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(0, 4))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(5, 9))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(9, 10))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(11, 14))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(15, 21))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(22, 25))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(26, 28))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(29, 35))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(35, 36))
        .save();

    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .withBounds(new SpanBounds(37, 70))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(37, 40))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(41, 44))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(45, 49))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(50, 57))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(58, 60))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(61, 65))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(66, 69))
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(69, 70))
        .save();

    Processor p =
        new NER.Processor(
            NERTest.class.getResourceAsStream("en-ner-person.bin"),
            AnnotationTypes.ANNOTATION_TYPE_PERSON);
    p.process(item);

    Map<String, Annotation> annotations = new HashMap<>();
    content
        .getAnnotations()
        .getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .forEach(a -> annotations.put(content.getText(a).get(), a));

    assertEquals(3, annotations.size());
    assertTrue(annotations.containsKey("Joe Bloggs"));
    assertTrue(annotations.containsKey("Joe"));
    assertTrue(annotations.containsKey("Jane Doe"));

    annotations.forEach(
        (s, a) -> {
          assertTrue(a.getProperties().get(PropertyKeys.PROPERTY_KEY_PROBABILITY).isPresent());
        });

    p.close();
  }
}
