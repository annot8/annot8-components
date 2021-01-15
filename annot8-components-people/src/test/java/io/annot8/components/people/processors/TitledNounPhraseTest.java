/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.people.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupRoles;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class TitledNounPhraseTest {
  @Test
  public void test() {
    try (Processor p = new TitledNounPhrase.Processor()) {
      Item item = new TestItem();

      Text content = item.createContent(TestStringContent.class).withData("Ms Joan Doe").save();

      AnnotationStore store = content.getAnnotations();

      Annotation a1 =
          store
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
              .withBounds(new SpanBounds(0, 2))
              .save();
      Annotation a2 =
          store
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
              .withBounds(new SpanBounds(3, 7))
              .save();
      Annotation a3 =
          store
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
              .withBounds(new SpanBounds(8, 11))
              .save();

      item.getGroups()
          .create()
          .withType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
          .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a1)
          .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a2)
          .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a3)
          .save();

      p.process(item);

      List<Annotation> annotations =
          store.getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON).collect(Collectors.toList());
      assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_PERSON, a.getType());
      assertEquals("female", a.getProperties().get(PropertyKeys.PROPERTY_KEY_GENDER).get());
      assertEquals("Ms", a.getProperties().get(PropertyKeys.PROPERTY_KEY_TITLE).get());
    }
  }

  @Test
  public void testMultiWordTitle() {
    try (Processor p = new TitledNounPhrase.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class).withData("Grand Duke Peter").save();

      AnnotationStore store = content.getAnnotations();

      Annotation a1 =
          store
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
              .withBounds(new SpanBounds(0, 5))
              .save();
      Annotation a2 =
          store
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
              .withBounds(new SpanBounds(6, 10))
              .save();
      Annotation a3 =
          store
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
              .withBounds(new SpanBounds(11, 16))
              .save();

      item.getGroups()
          .create()
          .withType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
          .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a1)
          .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a2)
          .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a3)
          .save();

      p.process(item);

      List<Annotation> annotations =
          store.getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON).collect(Collectors.toList());
      assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_PERSON, a.getType());
      assertEquals("male", a.getProperties().get(PropertyKeys.PROPERTY_KEY_GENDER).get());
      assertEquals("Grand Duke", a.getProperties().get(PropertyKeys.PROPERTY_KEY_TITLE).get());
    }
  }
}
