/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.groups.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

public class FilterGroupsByPropertyTest {
  @Test
  public void test() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("ABC").save();
    Annotation a1 =
        c1.getAnnotations().create().withType("type1").withBounds(new SpanBounds(0, 1)).save();
    Annotation a2 =
        c1.getAnnotations().create().withType("type2").withBounds(new SpanBounds(1, 2)).save();
    Annotation a3 =
        c1.getAnnotations().create().withType("type3").withBounds(new SpanBounds(2, 3)).save();

    TestStringContent c2 = item.createContent(TestStringContent.class).withData("AB").save();
    Annotation a4 =
        c2.getAnnotations().create().withType("type1").withBounds(new SpanBounds(0, 1)).save();
    Annotation a5 =
        c2.getAnnotations().create().withType("type2").withBounds(new SpanBounds(1, 2)).save();

    item.getGroups()
        .create()
        .withType("group-type1")
        .withAnnotation("member", a1)
        .withAnnotation("member", a4)
        .withProperty("count", 2)
        .withProperty("value", "A")
        .save();

    item.getGroups()
        .create()
        .withType("group-type2")
        .withAnnotation("member", a2)
        .withAnnotation("member", a5)
        .withProperty("count", 2)
        .withProperty("value", "B")
        .save();

    item.getGroups()
        .create()
        .withType("group-type3")
        .withAnnotation("member", a3)
        .withProperty("count", 1)
        .withProperty("value", "C")
        .save();

    FilterGroupsByProperty.Settings settings = new FilterGroupsByProperty.Settings("count", 2);
    assertTrue(settings.validate());

    FilterGroupsByProperty d = new FilterGroupsByProperty();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(0L, item.getGroups().getByType("group-type1").count());
    assertEquals(0L, item.getGroups().getByType("group-type2").count());
    assertEquals(1L, item.getGroups().getByType("group-type3").count());
  }
}
