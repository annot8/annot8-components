/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.components.cyber.processors.EpochTime.Settings;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EpochTimeTest {

  @Test
  public void testMillis() throws Annot8Exception {
    EpochTime et = new EpochTime();

    Settings settings = new Settings();
    settings.setMilliseconds(true);

    try (Processor p = et.createComponent(new SimpleContext(), settings)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("It happened at 1507725753567")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("1507725753567", a.getBounds().getData(content).get());
      Assertions.assertEquals(2, a.getProperties().getAll().size());
      Assertions.assertEquals("ms", a.getProperties().get(PropertyKeys.PROPERTY_KEY_UNIT).get());
      Assertions.assertEquals(
          Instant.ofEpochMilli(1507725753567L),
          a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    }
  }

  @Test
  public void testSeconds() throws Annot8Exception {
    EpochTime et = new EpochTime();

    Settings settings = new Settings();
    settings.setMilliseconds(false);

    try (Processor p = et.createComponent(new SimpleContext(), settings)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class).withData("It happened at 1507725753").save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("1507725753", a.getBounds().getData(content).get());
      Assertions.assertEquals(2, a.getProperties().getAll().size());
      Assertions.assertEquals("s", a.getProperties().get(PropertyKeys.PROPERTY_KEY_UNIT).get());
      Assertions.assertEquals(
          Instant.ofEpochSecond(1507725753L),
          a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    }
  }

  @Test
  public void testEarliest() throws Annot8Exception {
    EpochTime et = new EpochTime();

    Settings settings = new Settings();
    settings.setMilliseconds(false);
    settings.setEarliestTimestamp(Instant.ofEpochSecond(1600000000));

    try (Processor p = et.createComponent(new SimpleContext(), settings)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class).withData("It happened at 1507725753").save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(0, annotations.size());
    }
  }

  @Test
  public void testLatest() throws Annot8Exception {
    EpochTime et = new EpochTime();

    Settings settings = new Settings();
    settings.setMilliseconds(false);
    settings.setLatestTimestamp(Instant.ofEpochSecond(1400000000));

    try (Processor p = et.createComponent(new SimpleContext(), settings)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class).withData("It happened at 1507725753").save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(0, annotations.size());
    }
  }
}
