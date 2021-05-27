/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupRoles;
import io.annot8.conventions.GroupTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

public class FilterGrammarTest {
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
    Annotation w1 =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(0, 4))
            .save();
    Annotation w2 =
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
    Annotation w3 =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(11, 14))
            .save();
    Annotation w4 =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(15, 21))
            .save();
    Annotation joeBloggs =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .withBounds(new SpanBounds(11, 21))
            .save();
    Annotation w5 =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(22, 25))
            .save();
    Annotation w6 =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(26, 28))
            .save();
    Annotation w7 =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(29, 35))
            .save();
    Annotation london =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
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
    Annotation joe =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
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
    Annotation janeDoe =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .withBounds(new SpanBounds(61, 69))
            .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(69, 70))
        .save();

    item.getGroups()
        .create()
        .withType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, w1)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, w2)
        .save();
    item.getGroups()
        .create()
        .withType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, w3)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, w4)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, w5)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, w6)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, w7)
        .save();
    item.getGroups()
        .create()
        .withType(GroupTypes.GROUP_TYPE_GRAMMAR_COREFERENCE)
        .withAnnotation(GroupRoles.GROUP_ROLE_MENTION, joe)
        .withAnnotation(GroupRoles.GROUP_ROLE_MENTION, joeBloggs)
        .save();
    item.getGroups()
        .create()
        .withType(GroupTypes.GROUP_TYPE_EVENT)
        .withAnnotation(GroupRoles.GROUP_ROLE_PARTICIPANT, joeBloggs)
        .withAnnotation(GroupRoles.GROUP_ROLE_PARTICIPANT, janeDoe)
        .withAnnotation(GroupRoles.GROUP_ROLE_LOCATION, london)
        .save();

    Processor p = new FilterGrammar.Processor();
    p.process(item);
    p.close();

    assertEquals(
        3L, content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON).count());
    assertEquals(
        1L, content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_LOCATION).count());
    assertEquals(4L, content.getAnnotations().getAll().count());

    assertEquals(1L, item.getGroups().getByType(GroupTypes.GROUP_TYPE_EVENT).count());
    assertEquals(1L, item.getGroups().getByType(GroupTypes.GROUP_TYPE_GRAMMAR_COREFERENCE).count());
    assertEquals(2L, item.getGroups().getAll().count());
  }
}
