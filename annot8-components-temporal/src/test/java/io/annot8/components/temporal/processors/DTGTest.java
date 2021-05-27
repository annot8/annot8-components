/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.temporal.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.AnnotationCapability;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.capabilities.ContentCapability;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DTGTest {

  @Test
  public void testCapabilities() {
    DTG n = new DTG();

    // Get the capabilities and check that we have the expected number
    Capabilities c = n.capabilities();
    assertEquals(1, c.creates().count());
    assertEquals(1, c.processes().count());
    assertEquals(0, c.deletes().count());

    // Check that we're creating an Annotation and that it has the correct definitions
    AnnotationCapability annotCap = c.creates(AnnotationCapability.class).findFirst().get();
    assertEquals(SpanBounds.class, annotCap.getBounds());
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, annotCap.getType());

    // Check that we're processing a Content and that it has the correct definitions
    ContentCapability contentCap = c.processes(ContentCapability.class).findFirst().get();
    assertEquals(Text.class, ((ContentCapability) contentCap).getType());
  }

  @Test
  public void testCreateComponent() {
    DTG n = new DTG();

    // Test that we actually get a component when we create it
    Processor np = n.createComponent(null, NoSettings.getInstance());
    assertNotNull(np);
  }

  @Test
  public void testProcess1() {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("This test was written at 251137Z FEB 13")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("251137Z FEB 13", a.getBounds().getData(content).get());
      Assertions.assertEquals(1, a.getProperties().getAll().size());
      Object val = a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get();
      Assertions.assertTrue(val instanceof ZonedDateTime);
      ZonedDateTime zdt = (ZonedDateTime) val;
      ZonedDateTime date = ZonedDateTime.of(2013, 2, 25, 11, 37, 0, 0, ZoneOffset.UTC);
      Assertions.assertEquals(date, zdt);
    }
  }

  @Test
  public void testProcess2() {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Report Title: An example report\nDTG: 04 1558D Sep 10")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("04 1558D Sep 10", a.getBounds().getData(content).get());
      Assertions.assertEquals(1, a.getProperties().getAll().size());
      Object val = a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get();
      Assertions.assertTrue(val instanceof ZonedDateTime);
      ZonedDateTime zdt = (ZonedDateTime) val;
      ZonedDateTime date = ZonedDateTime.of(2010, 9, 4, 15, 58, 0, 0, ZoneOffset.ofHours(4));
      Assertions.assertEquals(date, zdt);
    }
  }

  @Test
  public void testProcess3() {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Report Title: An example report\nDTG: 04 1558D*SEP 10")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("04 1558D*SEP 10", a.getBounds().getData(content).get());
      Assertions.assertEquals(1, a.getProperties().getAll().size());
      Object val = a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get();
      Assertions.assertTrue(val instanceof ZonedDateTime);
      ZonedDateTime zdt = (ZonedDateTime) val;
      ZonedDateTime date =
          ZonedDateTime.of(2010, 9, 4, 15, 58, 0, 0, ZoneOffset.ofHoursMinutes(4, 30));
      Assertions.assertEquals(date, zdt);
    }
  }

  @Test
  public void testProcess4() {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Report Title: An example report\nDTG: 04/1558/Z/SEP/10")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("04/1558/Z/SEP/10", a.getBounds().getData(content).get());
      Assertions.assertEquals(1, a.getProperties().getAll().size());
      Object val = a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get();
      Assertions.assertTrue(val instanceof ZonedDateTime);
      ZonedDateTime zdt = (ZonedDateTime) val;
      ZonedDateTime date = ZonedDateTime.of(2010, 9, 4, 15, 58, 0, 0, ZoneOffset.UTC);
      Assertions.assertEquals(date, zdt);
    }
  }

  @Test
  public void testInvalid() {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("This data is invalid:  31 1558D*SEP 10")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(0, annotations.size());
    }
  }
}
