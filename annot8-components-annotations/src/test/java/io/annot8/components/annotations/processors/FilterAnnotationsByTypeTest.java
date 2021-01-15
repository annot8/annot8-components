/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FilterAnnotationsByTypeTest {
  @Test
  public void test() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("ABC").save();
    c1.getAnnotations().create().withType("type1").withBounds(new SpanBounds(0, 1)).save();
    c1.getAnnotations().create().withType("type1").withBounds(new SpanBounds(1, 2)).save();
    c1.getAnnotations().create().withType("type1").withBounds(new SpanBounds(2, 3)).save();
    c1.getAnnotations().create().withType("type2").withBounds(new SpanBounds(0, 2)).save();
    c1.getAnnotations().create().withType("type2").withBounds(new SpanBounds(1, 3)).save();
    c1.getAnnotations().create().withType("type3").withBounds(new SpanBounds(0, 3)).save();

    TestStringContent c2 = item.createContent(TestStringContent.class).withData("AB").save();
    c2.getAnnotations().create().withType("type1").withBounds(new SpanBounds(0, 1)).save();
    c2.getAnnotations().create().withType("type1").withBounds(new SpanBounds(1, 2)).save();
    c2.getAnnotations().create().withType("type2").withBounds(new SpanBounds(0, 2)).save();
    c2.getAnnotations().create().withType("type3").withBounds(new SpanBounds(0, 2)).save();

    FilterAnnotationsByType.Settings settings =
        new FilterAnnotationsByType.Settings(List.of("type1", "type2"));
    assertTrue(settings.validate());

    FilterAnnotationsByType d = new FilterAnnotationsByType();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    item.getContents()
        .forEach(
            c -> {
              assertEquals(0L, c.getAnnotations().getByType("type1").count());
              assertEquals(0L, c.getAnnotations().getByType("type2").count());
              assertEquals(1L, c.getAnnotations().getByType("type3").count());
            });
  }

  @Test
  public void testWildcard() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("ABC").save();
    c1.getAnnotations().create().withType("foo/bar").withBounds(new SpanBounds(0, 3)).save();
    c1.getAnnotations().create().withType("foo/baz").withBounds(new SpanBounds(0, 3)).save();
    c1.getAnnotations().create().withType("bar/baz").withBounds(new SpanBounds(0, 3)).save();

    FilterAnnotationsByType.Settings settings =
        new FilterAnnotationsByType.Settings(List.of("foo/*"));
    assertTrue(settings.validate());

    FilterAnnotationsByType d = new FilterAnnotationsByType();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    item.getContents()
        .forEach(
            c -> {
              assertEquals(1L, c.getAnnotations().getAll().count());
            });
  }
}
