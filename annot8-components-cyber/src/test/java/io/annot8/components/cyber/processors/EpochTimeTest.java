/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.Text;
import io.annot8.components.cyber.processors.EpochTime.EpochTimeSettings;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class EpochTimeTest {

  @Test
  public void testMillis() throws Annot8Exception {
    try (Processor p = new EpochTime()) {
      Item item = new TestItem();

      EpochTimeSettings settings = new EpochTimeSettings();
      settings.setMilliseconds(true);

      Context context = new TestContext(settings);

      p.configure(context);

      Text content =
          item.create(TestStringContent.class)
              .withName("test")
              .withData("It happened at 1507725753567")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TIMESTAMP, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("1507725753567", a.getBounds().getData(content).get());
      Assertions.assertEquals(2, a.getProperties().getAll().size());
      Assertions.assertEquals("ms", a.getProperties().get(PropertyKeys.PROPERTY_KEY_UNIT).get());
      Assertions.assertEquals(
          "1970-01-01T00:00:00.000Z",
          a.getProperties().get(PropertyKeys.PROPERTY_KEY_REFERENCE).get());
    }
  }

  @Test
  public void testSeconds() throws Annot8Exception {
    try (Processor p = new EpochTime()) {
      Item item = new TestItem();

      EpochTimeSettings settings = new EpochTimeSettings();
      settings.setMilliseconds(false);

      Context context = new TestContext(settings);

      p.configure(context);

      Text content =
          item.create(TestStringContent.class)
              .withName("test")
              .withData("It happened at 1507725753")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TIMESTAMP, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("1507725753", a.getBounds().getData(content).get());
      Assertions.assertEquals(2, a.getProperties().getAll().size());
      Assertions.assertEquals("s", a.getProperties().get(PropertyKeys.PROPERTY_KEY_UNIT).get());
      Assertions.assertEquals(
          "1970-01-01T00:00:00Z", a.getProperties().get(PropertyKeys.PROPERTY_KEY_REFERENCE).get());
    }
  }

  @Test
  public void testEarliest() throws Annot8Exception {
    try (Processor p = new EpochTime()) {
      Item item = new TestItem();

      EpochTimeSettings settings = new EpochTimeSettings();
      settings.setMilliseconds(false);
      settings.setEarliestTimestamp(Instant.ofEpochSecond(1600000000));

      Context context = new TestContext(settings);

      p.configure(context);

      Text content =
          item.create(TestStringContent.class)
              .withName("test")
              .withData("It happened at 1507725753")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(0, annotations.size());
    }
  }

  @Test
  public void testLatest() throws Annot8Exception {
    try (Processor p = new EpochTime()) {
      Item item = new TestItem();

      EpochTimeSettings settings = new EpochTimeSettings();
      settings.setMilliseconds(false);
      settings.setLatestTimestamp(Instant.ofEpochSecond(1400000000));

      Context context = new TestContext(settings);

      p.configure(context);

      Text content =
          item.create(TestStringContent.class)
              .withName("test")
              .withData("It happened at 1507725753")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(0, annotations.size());
    }
  }
}
