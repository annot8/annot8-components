/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.people.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class SingularPronounsTest {
  @Test
  public void testGender() {
    String text = "She met him last night";
    try (Processor p = new SingularPronouns.Processor()) {
      Item item = new TestItem();

      Text content = item.createContent(TestStringContent.class).withData(text).save();
      AnnotationStore store = content.getAnnotations();

      Matcher m = Pattern.compile("[a-z]+", Pattern.CASE_INSENSITIVE).matcher(text);
      while (m.find())
        store
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(m.start(), m.end()))
            .save();

      p.process(item);

      List<Annotation> annotations =
          store
              .getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
              .sorted(SortUtils.SORT_BY_SPANBOUNDS)
              .collect(Collectors.toList());
      assertEquals(2, annotations.size());

      Annotation a1 = annotations.get(0);
      assertEquals("She", a1.getBounds(SpanBounds.class).get().getData(content).orElse("ERROR"));
      assertEquals(1, a1.getProperties().getAll().size());
      assertEquals("female", a1.getProperties().get(PropertyKeys.PROPERTY_KEY_GENDER).get());

      Annotation a2 = annotations.get(1);
      assertEquals("him", a2.getBounds(SpanBounds.class).get().getData(content).orElse("ERROR"));
      assertEquals(1, a2.getProperties().getAll().size());
      assertEquals("male", a2.getProperties().get(PropertyKeys.PROPERTY_KEY_GENDER).get());
    }
  }

  @Test
  public void testAmbiguous() {
    String text = "I went to the park with you";
    try (Processor p = new SingularPronouns.Processor()) {
      Item item = new TestItem();

      Text content = item.createContent(TestStringContent.class).withData(text).save();
      AnnotationStore store = content.getAnnotations();

      Matcher m = Pattern.compile("[a-z]+", Pattern.CASE_INSENSITIVE).matcher(text);
      while (m.find())
        store
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(m.start(), m.end()))
            .save();

      p.process(item);

      List<Annotation> annotations =
          store
              .getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
              .sorted(SortUtils.SORT_BY_SPANBOUNDS)
              .collect(Collectors.toList());
      assertEquals(2, annotations.size());

      Annotation a1 = annotations.get(0);
      assertEquals("I", a1.getBounds(SpanBounds.class).get().getData(content).orElse("ERROR"));
      assertEquals(0, a1.getProperties().getAll().size());

      Annotation a2 = annotations.get(1);
      assertEquals("you", a2.getBounds(SpanBounds.class).get().getData(content).orElse("ERROR"));
      assertEquals(0, a2.getProperties().getAll().size());
    }
  }
}
