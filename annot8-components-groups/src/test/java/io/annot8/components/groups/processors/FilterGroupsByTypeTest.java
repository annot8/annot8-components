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
import java.util.List;
import org.junit.jupiter.api.Test;

public class FilterGroupsByTypeTest {
  @Test
  public void test() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("ABC").save();
    Annotation a1 =
        c1.getAnnotations().create().withType("type1").withBounds(new SpanBounds(0, 1)).save();
    Annotation a2 =
        c1.getAnnotations().create().withType("type1").withBounds(new SpanBounds(1, 2)).save();
    Annotation a3 =
        c1.getAnnotations().create().withType("type1").withBounds(new SpanBounds(2, 3)).save();
    Annotation a4 =
        c1.getAnnotations().create().withType("type2").withBounds(new SpanBounds(0, 2)).save();
    Annotation a5 =
        c1.getAnnotations().create().withType("type2").withBounds(new SpanBounds(1, 3)).save();
    Annotation a6 =
        c1.getAnnotations().create().withType("type3").withBounds(new SpanBounds(0, 3)).save();

    TestStringContent c2 = item.createContent(TestStringContent.class).withData("AB").save();
    Annotation a7 =
        c2.getAnnotations().create().withType("type1").withBounds(new SpanBounds(0, 1)).save();
    Annotation a8 =
        c2.getAnnotations().create().withType("type1").withBounds(new SpanBounds(1, 2)).save();
    Annotation a9 =
        c2.getAnnotations().create().withType("type2").withBounds(new SpanBounds(0, 2)).save();
    Annotation a10 =
        c2.getAnnotations().create().withType("type3").withBounds(new SpanBounds(0, 2)).save();

    item.getGroups()
        .create()
        .withType("group-type1")
        .withAnnotation("member", a1)
        .withAnnotation("member", a2)
        .withAnnotation("member", a3)
        .withAnnotation("member", a7)
        .withAnnotation("member", a8)
        .save();

    item.getGroups()
        .create()
        .withType("group-type2")
        .withAnnotation("member", a4)
        .withAnnotation("member", a5)
        .withAnnotation("member", a9)
        .save();

    item.getGroups()
        .create()
        .withType("group-type3")
        .withAnnotation("member", a6)
        .withAnnotation("member", a10)
        .save();

    FilterGroupsByType.Settings settings =
        new FilterGroupsByType.Settings(List.of("group-type1", "group-type3"));
    assertTrue(settings.validate());

    FilterGroupsByType d = new FilterGroupsByType();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(0L, item.getGroups().getByType("group-type1").count());
    assertEquals(1L, item.getGroups().getByType("group-type2").count());
    assertEquals(0L, item.getGroups().getByType("group-type3").count());
  }

  @Test
  public void testWildcard() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("ABC").save();
    Annotation a1 =
        c1.getAnnotations().create().withType("entity").withBounds(new SpanBounds(0, 1)).save();
    Annotation a2 =
        c1.getAnnotations().create().withType("entity").withBounds(new SpanBounds(1, 2)).save();
    Annotation a3 =
        c1.getAnnotations().create().withType("entity").withBounds(new SpanBounds(2, 3)).save();

    item.getGroups()
        .create()
        .withType("group/foo/bar")
        .withAnnotation("member", a1)
        .withAnnotation("member", a2)
        .save();

    item.getGroups()
        .create()
        .withType("group/foo/baz")
        .withAnnotation("member", a2)
        .withAnnotation("member", a3)
        .save();

    item.getGroups()
        .create()
        .withType("group/bar/baz")
        .withAnnotation("member", a1)
        .withAnnotation("member", a3)
        .save();

    FilterGroupsByType.Settings settings = new FilterGroupsByType.Settings(List.of("group/**/baz"));
    assertTrue(settings.validate());

    FilterGroupsByType d = new FilterGroupsByType();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(1L, item.getGroups().getByType("group/foo/bar").count());
    assertEquals(0L, item.getGroups().getByType("group/foo/baz").count());
    assertEquals(0L, item.getGroups().getByType("group/bar/baz").count());
  }
}
