/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FilterAnnotationsByPropertyTest {
  @Test
  public void test() {
    Item item = new TestItem();

    TestStringContent c1 = new TestStringContent(item);
    c1.setData("ABC");
    c1.getAnnotations()
        .create()
        .withType("type1")
        .withProperty("foo", 1)
        .withProperty("value", "A")
        .withBounds(new SpanBounds(0, 1))
        .save();
    c1.getAnnotations()
        .create()
        .withType("type2")
        .withProperty("foo", 1)
        .withProperty("value", "B")
        .withBounds(new SpanBounds(1, 2))
        .save();
    c1.getAnnotations()
        .create()
        .withType("type3")
        .withProperty("foo", 2)
        .withProperty("value", "C")
        .withBounds(new SpanBounds(2, 3))
        .save();

    TestStringContent c2 = new TestStringContent(item);
    c2.setData("AB");
    c2.getAnnotations()
        .create()
        .withType("type1")
        .withProperty("value", "A")
        .withBounds(new SpanBounds(0, 1))
        .save();
    c2.getAnnotations()
        .create()
        .withType("type2")
        .withProperty("value", "B")
        .withBounds(new SpanBounds(1, 2))
        .save();

    FilterAnnotationsByProperty.Settings settings =
        new FilterAnnotationsByProperty.Settings("value", "B");
    assertTrue(settings.validate());

    FilterAnnotationsByProperty d = new FilterAnnotationsByProperty();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    item.getContents()
        .forEach(
            c -> {
              assertEquals(1L, c.getAnnotations().getByType("type1").count());
              assertEquals(0L, c.getAnnotations().getByType("type2").count());
              if (c.getData().equals("ABC"))
                assertEquals(1L, c.getAnnotations().getByType("type3").count());
            });
  }
}
