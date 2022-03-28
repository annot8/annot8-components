/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FilterAnnotationsBySpanTest {

  @Test
  public void test() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("ABC").save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(0, 1)).save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(1, 2)).save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(2, 3)).save();

    TestStringContent c2 = item.createContent(TestStringContent.class).withData("AB").save();
    c2.getAnnotations().create().withType("letter").withBounds(new SpanBounds(0, 1)).save();
    c2.getAnnotations().create().withType("letter").withBounds(new SpanBounds(1, 2)).save();

    FilterAnnotationsBySpan.Settings settings =
        new FilterAnnotationsBySpan.Settings(List.of("B"), true, false);
    assertTrue(settings.validate());

    FilterAnnotationsBySpan d = new FilterAnnotationsBySpan();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    item.getContents(Text.class)
        .forEach(
            c -> {
              assertEquals(
                  0L,
                  c.getAnnotations()
                      .getByBounds(SpanBounds.class)
                      .filter(a -> c.getText(a).orElse("**BAD**").equals("B"))
                      .count());
              assertEquals(
                  1L,
                  c.getAnnotations()
                      .getByBounds(SpanBounds.class)
                      .filter(a -> c.getText(a).orElse("**BAD**").equals("A"))
                      .count());
            });
  }

  @Test
  public void testIgnoreCase() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("ABC").save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(0, 1)).save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(1, 2)).save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(2, 3)).save();

    TestStringContent c2 = item.createContent(TestStringContent.class).withData("AB").save();
    c2.getAnnotations().create().withType("letter").withBounds(new SpanBounds(0, 1)).save();
    c2.getAnnotations().create().withType("letter").withBounds(new SpanBounds(1, 2)).save();

    FilterAnnotationsBySpan.Settings settings =
        new FilterAnnotationsBySpan.Settings(List.of("b"), false, false);
    assertTrue(settings.validate());

    FilterAnnotationsBySpan d = new FilterAnnotationsBySpan();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    item.getContents(Text.class)
        .forEach(
            c -> {
              assertEquals(
                  0L,
                  c.getAnnotations()
                      .getByBounds(SpanBounds.class)
                      .filter(a -> c.getText(a).orElse("**BAD**").equals("B"))
                      .count());
              assertEquals(
                  1L,
                  c.getAnnotations()
                      .getByBounds(SpanBounds.class)
                      .filter(a -> c.getText(a).orElse("**BAD**").equals("A"))
                      .count());
            });
  }

  @Test
  public void testRegex() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("ABC").save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(0, 1)).save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(1, 2)).save();
    c1.getAnnotations().create().withType("letter").withBounds(new SpanBounds(2, 3)).save();

    TestStringContent c2 = item.createContent(TestStringContent.class).withData("AB").save();
    c2.getAnnotations().create().withType("letter").withBounds(new SpanBounds(0, 1)).save();
    c2.getAnnotations().create().withType("letter").withBounds(new SpanBounds(1, 2)).save();

    FilterAnnotationsBySpan.Settings settings =
        new FilterAnnotationsBySpan.Settings(List.of("[BDE]"), false, true);
    assertTrue(settings.validate());

    FilterAnnotationsBySpan d = new FilterAnnotationsBySpan();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    Processor p = d.createComponent(null, settings);

    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    item.getContents(Text.class)
        .forEach(
            c -> {
              assertEquals(
                  0L,
                  c.getAnnotations()
                      .getByBounds(SpanBounds.class)
                      .filter(a -> c.getText(a).orElse("**BAD**").equals("B"))
                      .count());
              assertEquals(
                  1L,
                  c.getAnnotations()
                      .getByBounds(SpanBounds.class)
                      .filter(a -> c.getText(a).orElse("**BAD**").equals("A"))
                      .count());
            });
  }
}
