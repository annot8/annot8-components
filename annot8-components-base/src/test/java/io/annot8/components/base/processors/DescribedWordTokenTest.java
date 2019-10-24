/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.annotations.Annotation;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.components.stopwords.resources.CollectionStopwords;
import io.annot8.components.stopwords.resources.NoOpStopwords;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class DescribedWordTokenTest {
  @Test
  public void test() {
    TestItem item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class)
            .withData("Tom ate a big, red apple. Green bananas aren't nice.")
            .save();

    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(0, 25))
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(0, 3))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(4, 7))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(8, 9))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(10, 13))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(15, 18))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(19, 24))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();

    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(25, 52))
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(26, 31))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(32, 39))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(40, 46))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(47, 51))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();

    DescribedWordToken.Processor p =
        new DescribedWordToken.Processor(
            new NoOpStopwords(),
            "fruit",
            Set.of("APPLES", "APPLE", "BANANAS", "BANANA", "CHERRIES", "CHERRY"),
            Set.of("big", "small", "juicy", "ripe", "unripe", "red", "green"),
            false,
            Collections.emptyMap());
    p.process(item);

    List<Annotation> a =
        content
            .getAnnotations()
            .getByBoundsAndType(SpanBounds.class, "fruit")
            .sorted(SortUtils.SORT_BY_SPANBOUNDS)
            .collect(Collectors.toList());
    assertEquals(2, a.size());

    assertEquals("big, red apple", content.getText(a.get(0)).get());
    assertEquals("Green bananas", content.getText(a.get(1)).get());
  }

  @Test
  public void testStopwords() {
    TestItem item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class)
            .withData("A big juicy and ripe apple and an unripe, green banana.")
            .save();

    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(0, 55))
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(0, 1))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(2, 5))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(6, 11))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(12, 15))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(16, 20))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(21, 26))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(27, 30))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(31, 33))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(34, 40))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(42, 47))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();
    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(48, 54))
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .save();

    DescribedWordToken.Processor p =
        new DescribedWordToken.Processor(
            new CollectionStopwords("en", Set.of("a", "an", "and")),
            "fruit",
            Set.of("APPLE", "BANANA"),
            Set.of("big", "small", "juicy", "ripe", "unripe", "red", "green"),
            false,
            Collections.emptyMap());
    p.process(item);

    List<Annotation> a =
        content
            .getAnnotations()
            .getByBoundsAndType(SpanBounds.class, "fruit")
            .sorted(SortUtils.SORT_BY_SPANBOUNDS)
            .collect(Collectors.toList());
    assertEquals(2, a.size());

    assertEquals("big juicy and ripe apple", content.getText(a.get(0)).get());
    assertEquals("unripe, green banana", content.getText(a.get(1)).get());
  }
}
