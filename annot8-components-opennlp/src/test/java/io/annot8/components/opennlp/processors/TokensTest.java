/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class TokensTest {
  @Test
  public void testBuiltInWithSentences() {
    Item item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class)
            .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Dr. Jane Doe.")
            .save();

    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(0, 36))
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(37, 74))
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .save();

    Tokens tokens = new Tokens();
    Processor p = tokens.createComponent(null, new Tokens.Settings());

    p.process(item);

    List<String> annotations = new ArrayList<>();
    content
        .getAnnotations()
        .getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .forEach(a -> annotations.add(content.getText(a).get()));

    assertEquals(18, annotations.size());

    p.close();
  }

  @Test
  public void testBuiltInWithoutSentences() {
    Item item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class)
            .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Dr. Jane Doe.")
            .save();

    Tokens tokens = new Tokens();
    Processor p = tokens.createComponent(null, new Tokens.Settings());

    p.process(item);

    List<String> annotations = new ArrayList<>();
    content
        .getAnnotations()
        .getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .forEach(a -> annotations.add(content.getText(a).get()));

    assertEquals(18, annotations.size());

    p.close();
  }
}
