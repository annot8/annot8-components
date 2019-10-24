/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.types.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ChangeTypeTest {

  @Test
  public void testChangeTypeRetain() throws Annot8Exception {

    ChangeType.Settings cts = new ChangeType.Settings("my.person", "Person", true);
    assertTrue(cts.validate());

    ChangeType ct = new ChangeType();
    Processor p = ct.createComponent(new SimpleContext(), cts);

    Item item = new TestItem();
    Text content =
        item.createContent(Text.class).withData("James went to London").withId("1").save();

    content
        .getAnnotations()
        .create()
        .withType("my.person")
        .withProperty("gender", "M")
        .withBounds(new SpanBounds(0, 5))
        .save();
    content
        .getAnnotations()
        .create()
        .withType("my.place")
        .withBounds(new SpanBounds(14, 20))
        .save();

    p.process(item);

    assertEquals(1, item.getContents().count());
    AnnotationStore as = item.getContent("1").get().getAnnotations();
    assertEquals(3, as.getAll().count());
    assertEquals(1, as.getByType("my.person").count());
    assertEquals(1, as.getByType("my.place").count());

    List<Annotation> person = as.getByType("Person").collect(Collectors.toList());
    assertEquals(1, person.size());
    Annotation a = person.get(0);

    assertEquals("James", a.getBounds().getData(content).get());
    assertEquals("M", a.getProperties().get("gender").get());

    p.close();
  }

  @Test
  public void testChangeTypeNoRetain() throws Annot8Exception {

    ChangeType.Settings cts = new ChangeType.Settings("my.person", "Person");
    assertTrue(cts.validate());

    ChangeType ct = new ChangeType();
    Processor p = ct.createComponent(new SimpleContext(), cts);

    Item item = new TestItem();
    Text content =
        item.createContent(Text.class).withData("James went to London").withId("1").save();

    content
        .getAnnotations()
        .create()
        .withType("my.person")
        .withProperty("gender", "M")
        .withBounds(new SpanBounds(0, 5))
        .save();
    content
        .getAnnotations()
        .create()
        .withType("my.place")
        .withBounds(new SpanBounds(14, 20))
        .save();

    p.process(item);

    assertEquals(1, item.getContents().count());
    AnnotationStore as = item.getContent("1").get().getAnnotations();
    assertEquals(2, as.getAll().count());
    assertEquals(0, as.getByType("my.person").count());
    assertEquals(1, as.getByType("my.place").count());

    List<Annotation> person = as.getByType("Person").collect(Collectors.toList());
    assertEquals(1, person.size());
    Annotation a = person.get(0);

    assertEquals("James", a.getBounds().getData(content).get());
    assertEquals("M", a.getProperties().get("gender").get());

    p.close();
  }
}
