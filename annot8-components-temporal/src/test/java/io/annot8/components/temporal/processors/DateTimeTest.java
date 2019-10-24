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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateTimeTest {

  @Test
  public void testCapabilities() {
    DateTime n = new DateTime();

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
    DateTime n = new DateTime();

    // Test that we actually get a component when we create it
    DateTime.Processor np = n.createComponent(null, NoSettings.getInstance());
    assertNotNull(np);
  }

  void testSingleAnnotation(
      String text, String annotation, Temporal instant) {
    try (Processor p = new DateTime.Processor()) {
      Item item = new TestItem();

      //    Item item = new SimpleItem(itemFactory, contentBuilderFactoryRegistry);
      Text content = item.createContent(TestStringContent.class).withData(text).save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals(annotation, a.getBounds().getData(content).get());
      Assertions.assertEquals(1, a.getProperties().getAll().size());

      Assertions.assertEquals(instant, a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    }
  }

  @Test
  public void testIso1() throws Annot8Exception {

    ZonedDateTime zdt = ZonedDateTime.of(2016, 10, 5, 11, 07, 22, 0, ZoneOffset.UTC);

    testSingleAnnotation(
        "It is currently 2016-10-05T11:07:22Z", "2016-10-05T11:07:22Z", zdt);
  }

  @Test
  public void testIso2() throws Annot8Exception {

    LocalDateTime ldt = LocalDateTime.of(2016, 10, 5, 11, 07, 22);

    testSingleAnnotation(
        "It is currently 2016-10-05T11:07:22", "2016-10-05T11:07:22", ldt);
  }

  @Test
  public void testIso3() throws Annot8Exception {

    ZonedDateTime zdt =
        ZonedDateTime.of(2016, 10, 5, 13, 37, 22, 0, ZoneOffset.ofHoursMinutes(2, 30));

    testSingleAnnotation(
        "It is currently 2016-10-05T13:37:22+02:30",
        "2016-10-05T13:37:22+02:30",
        zdt);
  }

  @Test
  public void testIso4() throws Annot8Exception {

    LocalDateTime ldt = LocalDateTime.of(2016, 10, 5, 11, 07, 22, 234000000);

    testSingleAnnotation(
        "It is currently 2016-10-05T11:07:22.234",
        "2016-10-05T11:07:22.234",
        ldt);
  }

  @Test
  public void testTimeOnDate1() throws Annot8Exception {

    LocalDateTime ldt = LocalDateTime.of(2016, 10, 5, 11, 0);

    testSingleAnnotation(
        "Be ready to go at 1100hrs on 5 October 2016",
        "1100hrs on 5 October 2016",
        ldt);
  }

  @Test
  public void testTimeOnDate2() throws Annot8Exception {

    LocalDateTime ldt = LocalDateTime.of(2016, 10, 5, 11, 0, 0);

    testSingleAnnotation(
        "Be ready to go at 11:00:00hrs on 5 Oct 2016",
        "11:00:00hrs on 5 Oct 2016",
        ldt);
  }

  @Test
  public void testDayMonthTime1() throws Annot8Exception {

    ZonedDateTime zdt = ZonedDateTime.of(2014, 4, 22, 15, 29, 0, 0, ZoneId.of("GMT"));

    testSingleAnnotation(
        "It happened at 22 Apr 2014 1529Z", "22 Apr 2014 1529Z", zdt);
  }

  @Test
  public void testDayMonthTime2() throws Annot8Exception {

    ZonedDateTime zdt = ZonedDateTime.of(2014, 4, 22, 15, 29, 0, 0, ZoneOffset.ofHours(-5));

    testSingleAnnotation(
        "It happened at 22 April 2014 1529 EST", "22 April 2014 1529 EST", zdt);
  }

  @Test
  public void testDayMonthTime3() throws Annot8Exception {

    ZonedDateTime zdt = ZonedDateTime.of(2014, 4, 22, 15, 29, 47, 0, ZoneId.of("GMT"));

    testSingleAnnotation(
        "It happened at 22 April 2014 152947Z", "22 April 2014 152947Z", zdt);
  }

  @Test
  public void testDayMonthTime4() throws Annot8Exception {

    LocalDateTime ldt = LocalDateTime.of(2014, 4, 22, 15, 29, 47);

    testSingleAnnotation(
        "It happened at 22 April 2014 15:29:47", "22 April 2014 15:29:47", ldt);
  }

  @Test
  public void testMonthDayTime1() throws Annot8Exception {

    ZonedDateTime zdt = ZonedDateTime.of(2014, 4, 22, 15, 29, 0, 0, ZoneId.of("GMT"));

    testSingleAnnotation(
        "It happened at Apr 22, 2014 1529Z", "Apr 22, 2014 1529Z", zdt);
  }

  @Test
  public void testMonthDayTime2() throws Annot8Exception {

    ZonedDateTime zdt = ZonedDateTime.of(2014, 4, 22, 15, 29, 0, 0, ZoneOffset.ofHours(-5));

    testSingleAnnotation(
        "It happened at April 22 2014 1529 EST", "April 22 2014 1529 EST", zdt);
  }
}
