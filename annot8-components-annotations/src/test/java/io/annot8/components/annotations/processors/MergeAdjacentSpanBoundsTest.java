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

public class MergeAdjacentSpanBoundsTest {
  @Test
  public void test() {
    Item item = new TestItem();

    TestStringContent c =
        item.createContent(TestStringContent.class)
            .withData("John Doe visited Ms.\tAlice Bethany  Cox with Jane last week.")
            .save();

    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(0, 4))
        .withProperty("surname", false)
        .save(); // John
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(5, 8))
        .withProperty("surname", true)
        .save(); // Doe
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(17, 28))
        .save(); // Ms. Alice B
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(21, 26))
        .save(); // Alice
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(27, 34))
        .save(); // Bethany
    c.getAnnotations().create().withType("person").withBounds(new SpanBounds(36, 39)).save(); // Cox
    c.getAnnotations()
        .create()
        .withType("person")
        .withBounds(new SpanBounds(45, 49))
        .save(); // Jane
    c.getAnnotations().create().withType("time").withBounds(new SpanBounds(50, 54)).save(); // last
    c.getAnnotations().create().withType("time").withBounds(new SpanBounds(55, 59)).save(); // week

    MergeAdjacentSpanBounds.Settings settings = new MergeAdjacentSpanBounds.Settings();
    settings.setTypes(Set.of("person", "location"));
    settings.setAllowableSeparators(Set.of(" ", "\t", "-"));
    settings.setMaxRepeatableSeparators(-1);

    Processor p = new MergeAdjacentSpanBounds.Processor(settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    Content<?> cProcessed = item.getContents().findFirst().get();

    List<Annotation> persons =
        cProcessed.getAnnotations().getByType("person").collect(Collectors.toList());
    List<Annotation> time =
        cProcessed.getAnnotations().getByType("time").collect(Collectors.toList());

    assertEquals(3, persons.size());
    assertEquals(2, time.size());

    List<String> personNames =
        persons.stream().map(a -> a.getBounds().getData(c).get()).collect(Collectors.toList());
    assertTrue(personNames.contains("John Doe"));
    assertTrue(personNames.contains("Ms.\tAlice Bethany  Cox"));
    assertTrue(personNames.contains("Jane"));

    List<Annotation> surnameProp =
        persons.stream().filter(a -> a.getProperties().has("surname")).collect(Collectors.toList());
    assertEquals(1, surnameProp.size());
    assertEquals(true, surnameProp.get(0).getProperties().get("surname").get());

    List<String> timeValues =
        time.stream().map(a -> a.getBounds().getData(c).get()).collect(Collectors.toList());
    assertTrue(timeValues.contains("last"));
    assertTrue(timeValues.contains("week"));
  }
}
