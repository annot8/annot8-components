/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.people.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.annotations.Annotation;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class PersonAgeTest {
  @Test
  public void test() {
    testSingle("Alice, 33, was seen in London.", 0, 5, 33);
    testSingle("She met up with Bob, 29.", 16, 19, 29);
    testSingle("Charlie (48 y/o) was seen nearby,", 0, 7, 48);
    testSingle("As was David (24 years).", 7, 12, 24);
    testSingle("Eve (19) was not seen", 0, 8, 19, 0, 3);
    testSingle("Nor was Fred, who is (20)", 8, 12, null);
  }

  private void testSingle(String sentence, int personBegin, int personEnd, Integer expectedAge) {
    testSingle(sentence, personBegin, personEnd, expectedAge, personBegin, personEnd);
  }

  private void testSingle(
      String sentence,
      int personBegin,
      int personEnd,
      Integer expectedAge,
      int expectedPersonBegin,
      int expectedPersonEnd) {
    TestItem item = new TestItem();
    Text text = item.createContent(Text.class).withData(sentence).save();

    text.getAnnotations()
        .create()
        .withBounds(new SpanBounds(personBegin, personEnd))
        .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .save();

    try (PersonAge.Processor p = new PersonAge.Processor()) {

      p.process(text);

      List<Annotation> annotations = text.getAnnotations().getAll().collect(Collectors.toList());
      assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_PERSON, a.getType());
      assertEquals(
          new SpanBounds(expectedPersonBegin, expectedPersonEnd),
          a.getBounds(SpanBounds.class).orElse(null));
      assertEquals(expectedAge, a.getProperties().get(PropertyKeys.PROPERTY_KEY_AGE).orElse(null));
    }
  }
}
