/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class MergeContainedSpanBoundsTest {

  @Test
  public void test() {
    Item item = new TestItem();

    TestStringContent c =
        item.createContent(TestStringContent.class)
            .withData("John Fitzgerald Kennedy was shot in Dallas.")
            .save();

    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(0, 4))
        .withProperty("a", 1)
        .withProperty("b", "b")
        .save(); // John
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(0, 15))
        .withProperty("a", 2)
        .withProperty("b", 2)
        .save(); // John Fitzgerald
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(0, 23))
        .withProperty("a", 3)
        .withProperty("c", "c")
        .save(); // John Fitzgerald Kennedy
    c.getAnnotations()
        .create()
        .withType("location")
        .withBounds(new SpanBounds(36, 42))
        .save(); // Dallas

    MergeContainedSpanBounds.Settings settings = new MergeContainedSpanBounds.Settings();
    settings.setTypes(Set.of("person", "location"));

    try (Processor p = new MergeContainedSpanBounds.Processor(settings)) {

      ProcessorResponse pr = p.process(item);
      assertEquals(ProcessorResponse.ok(), pr);

      Content<?> cProcessed = item.getContents().findFirst().get();

      List<Annotation> persons =
          cProcessed.getAnnotations().getByType("person").collect(Collectors.toList());
      List<Annotation> locations =
          cProcessed.getAnnotations().getByType("location").collect(Collectors.toList());

      assertEquals(1, persons.size());
      assertEquals(1, locations.size());

      Annotation person = persons.get(0);

      assertEquals("John Fitzgerald Kennedy", person.getBounds().getData(c).get());
      assertEquals("Dallas", locations.get(0).getBounds().getData(c).get());

      assertEquals(3, person.getProperties().get("a").orElse(null));
      assertEquals(2, person.getProperties().get("b").orElse(null));
      assertEquals("c", person.getProperties().get("c").orElse(null));
    }
  }

  @Test
  public void testOverlap() {
    Item item = new TestItem();

    TestStringContent c =
        item.createContent(TestStringContent.class)
            .withData("John Fitzgerald Kennedy was shot in Dallas.")
            .save();

    c.getAnnotations().create().withType("person").withBounds(new SpanBounds(0, 15)).save(); // John
    // Fitzgerald
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(5, 23))
        .save(); // Fitzgerald
    // Kennedy
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(5, 15))
        .save(); // Fitzgerald
    c.getAnnotations()
        .create()
        .withType("location")
        .withBounds(new SpanBounds(36, 42))
        .save(); // Dallas

    MergeContainedSpanBounds.Settings settings = new MergeContainedSpanBounds.Settings();
    settings.setTypes(Set.of("person", "location"));

    try (Processor p = new MergeContainedSpanBounds.Processor(settings)) {

      ProcessorResponse pr = p.process(item);
      assertEquals(ProcessorResponse.ok(), pr);

      Content<?> cProcessed = item.getContents().findFirst().get();

      List<Annotation> persons =
          cProcessed.getAnnotations().getByType("person").collect(Collectors.toList());
      List<Annotation> locations =
          cProcessed.getAnnotations().getByType("location").collect(Collectors.toList());

      assertEquals(2, persons.size());
      assertEquals(1, locations.size());

      List<String> personNames =
          persons.stream().map(a -> a.getBounds().getData(c).get()).collect(Collectors.toList());
      assertTrue(personNames.contains("John Fitzgerald"));
      assertTrue(personNames.contains("Fitzgerald Kennedy"));

      assertEquals("Dallas", locations.get(0).getBounds().getData(c).get());
    }
  }
}
