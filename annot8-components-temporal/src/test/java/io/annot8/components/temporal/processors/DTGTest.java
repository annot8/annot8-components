/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.annot8.components.temporal.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.AnnotationCapability;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.capabilities.ContentCapability;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    assertEquals(SpanBounds.class, ((AnnotationCapability) annotCap).getBounds());
    assertEquals(
        AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, ((AnnotationCapability) annotCap).getType());

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
  public void testProcess1() throws Annot8Exception {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      //    Item item = new SimpleItem(itemFactory, contentBuilderFactoryRegistry);
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
  public void testProcess2() throws Annot8Exception {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      //    Item item = new SimpleItem(itemFactory, contentBuilderFactoryRegistry);
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
  public void testProcess3() throws Annot8Exception {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      //    Item item = new SimpleItem(itemFactory, contentBuilderFactoryRegistry);
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
  public void testInvalid() throws Annot8Exception {
    try (Processor p = new DTG.Processor()) {
      Item item = new TestItem();

      //    Item item = new SimpleItem(itemFactory, contentBuilderFactoryRegistry);
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
