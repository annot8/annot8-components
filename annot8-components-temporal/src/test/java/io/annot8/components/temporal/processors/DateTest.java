/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.temporal.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.AnnotationCapability;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.capabilities.ContentCapability;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class DateTest {

  @Test
  public void testCapabilities() {
    Date n = new Date();

    // Get the capabilities and check that we have the expected number
    Capabilities c = n.capabilities();
    assertEquals(2, c.creates().count());
    assertEquals(1, c.processes().count());
    assertEquals(0, c.deletes().count());

    assertTrue(
        c.creates(AnnotationCapability.class)
            .allMatch(cap -> cap.getType().startsWith(AnnotationTypes.TEMPORAL_PREFIX)));

    // Check that we're processing a Content and that it has the correct definitions
    ContentCapability contentCap = c.processes(ContentCapability.class).findFirst().get();
    assertEquals(Text.class, ((ContentCapability) contentCap).getType());
  }

  @Test
  public void testCreateComponent() {
    Date n = new Date();

    // Test that we actually get a component when we create it
    Date.Processor np = n.createComponent(null, new Date.Settings());
    assertNotNull(np);
  }

  public void testAnnotationInterval(
      Text content, Annotation a, String coveredText, Temporal start, Temporal end) {
    Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INTERVAL, a.getType());
    Assertions.assertEquals(content.getId(), a.getContentId());
    Assertions.assertEquals(coveredText, a.getBounds().getData(content).get());

    Assertions.assertEquals(2, a.getProperties().getAll().size());

    Assertions.assertEquals(start, a.getProperties().get(PropertyKeys.PROPERTY_KEY_START).get());
    Assertions.assertEquals(end, a.getProperties().get(PropertyKeys.PROPERTY_KEY_END).get());
  }

  public void testAnnotationInstant(
      Text content, Annotation a, String coveredText, Temporal instant) {
    Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
    Assertions.assertEquals(content.getId(), a.getContentId());
    Assertions.assertEquals(coveredText, a.getBounds().getData(content).get());

    Assertions.assertEquals(1, a.getProperties().getAll().size());

    Assertions.assertEquals(instant, a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
  }

  @Test
  public void testYears() {

    try (Processor p = new Date.Processor(false)) {
      Item item = new TestItem();

      // 1969 is too early o be picked up as a date
      // '6 is not pickde up as 2 digit years are not detected
      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "Woolworths was a retail chain from 1909-2008. We had very hot summers in 2009-11. 1969 was wet, as was the year '16.")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      Assertions.assertEquals(2, annotations.size());

      int i = 0;

      testAnnotationInterval(
          content, annotations.get(i++), "1909-2008", Year.of(1909), Year.of(2008));

      testAnnotationInterval(
          content, annotations.get(i++), "2009-11", Year.of(2009), Year.of(2011));
    }
  }

  @Test
  public void testMonthYears() {

    try (Processor p = new Date.Processor(false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "From January to November 2015, not a lot happened. From December 15-January '16, Christmas happened.")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      Assertions.assertEquals(2, annotations.size());

      int i = 0;

      testAnnotationInterval(
          content,
          annotations.get(i++),
          "January to November 2015",
          YearMonth.of(2015, 1),
          YearMonth.of(2015, 11));

      testAnnotationInterval(
          content,
          annotations.get(i++),
          "December 15-January '16",
          YearMonth.of(2015, 12),
          YearMonth.of(2016, 1));
    }
  }

  @Test
  public void testDayMonthYears() {

    try (Processor p = new Date.Processor(false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "He is on duty from 3-10 October 2016, whilst she was on duty 27th September - Monday 3 Oct 16. The Christmas break fell between 21st December 2016 and 2 January 17. On 2/3 January '17 there was a storm, and it rained on 2nd and 5th January 2017.")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      Assertions.assertEquals(6, annotations.size());

      int i = 0;

      testAnnotationInterval(
          content,
          annotations.get(i++),
          "3-10 October 2016",
          LocalDate.of(2016, 10, 3),
          LocalDate.of(2016, 10, 10));

      testAnnotationInterval(
          content,
          annotations.get(i++),
          "27th September - Monday 3 Oct 16",
          LocalDate.of(2016, 9, 27),
          LocalDate.of(2016, 10, 3));
      testAnnotationInterval(
          content,
          annotations.get(i++),
          "21st December 2016 and 2 January 17",
          LocalDate.of(2016, 12, 21),
          LocalDate.of(2017, 1, 2));

      testAnnotationInterval(
          content,
          annotations.get(i++),
          "2/3 January '17",
          LocalDate.of(2017, 1, 2),
          LocalDate.of(2017, 1, 3));
      testAnnotationInstant(
          content,
          annotations.get(i++),
          "2nd and 5th January 2017",
          LocalDate.of(2017, 1, 2)); // This is 2nd Jan 2017, despite covered text

      testAnnotationInstant(
          content, annotations.get(i++), "5th January 2017", LocalDate.of(2017, 1, 5));
    }
  }

  @Test
  public void testBadDayMonthYears() {

    try (Processor p = new Date.Processor(false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("She worked from 1st - 30th February 2015")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      Assertions.assertEquals(1, annotations.size());

      testAnnotationInstant(content, annotations.get(0), "February 2015", YearMonth.of(2015, 2));
    }
  }

  @Test
  public void testDates() {

    try (Processor p = new Date.Processor(false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "Today is Tuesday 4th October 2016, or October 4 2016, or 2016-10-04, or maybe even 4/10/16.")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      Assertions.assertEquals(4, annotations.size());

      int i = 0;

      testAnnotationInstant(
          content, annotations.get(i++), "Tuesday 4th October 2016", LocalDate.of(2016, 10, 4));

      testAnnotationInstant(
          content, annotations.get(i++), "October 4 2016", LocalDate.of(2016, 10, 4));

      testAnnotationInstant(content, annotations.get(i++), "2016-10-04", LocalDate.of(2016, 10, 4));

      testAnnotationInstant(content, annotations.get(i++), "4/10/16", LocalDate.of(2016, 10, 4));
    }
  }

  @Test
  public void testAmericanDates() {

    try (Processor p = new Date.Processor(true)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Is it 04/07/2017, or 07/04/2017? It could even be 23/12/2017!")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      Assertions.assertEquals(3, annotations.size());

      int i = 0;

      testAnnotationInstant(content, annotations.get(i++), "04/07/2017", LocalDate.of(2017, 4, 7));

      testAnnotationInstant(content, annotations.get(i++), "07/04/2017", LocalDate.of(2017, 7, 4));

      testAnnotationInstant(
          content, annotations.get(i++), "23/12/2017", LocalDate.of(2017, 12, 23));
    }
  }

  public void testOne(Processor p, String text, String covered, Temporal start, Temporal end) {
    Item item = new TestItem();

    Text content = item.createContent(TestStringContent.class).withData(text).save();

    p.process(item);

    AnnotationStore store = content.getAnnotations();
    List<Annotation> annotations = store.getAll().collect(Collectors.toList());

    Assertions.assertEquals(1, annotations.size());

    testAnnotationInterval(content, annotations.get(0), covered, start, end);
  }

  public void testOne(Processor p, String text, String covered, Temporal instant) {
    Item item = new TestItem();

    Text content = item.createContent(TestStringContent.class).withData(text).save();

    p.process(item);

    AnnotationStore store = content.getAnnotations();
    List<Annotation> annotations = store.getAll().collect(Collectors.toList());

    Assertions.assertEquals(1, annotations.size());

    testAnnotationInstant(content, annotations.get(0), covered, instant);
  }

  @Test
  public void testMonth() {

    try (Processor p = new Date.Processor(false)) {
      testOne(
          p,
          "It was during February 2015 that the event happened",
          "February 2015",
          YearMonth.of(2015, 2));
    }
    try (Processor p = new Date.Processor(false)) {
      testOne(
          p,
          "It was during early February 2015 that the event happened",
          "early February 2015",
          LocalDate.of(2015, 2, 1),
          LocalDate.of(2015, 2, 10));
    }
    try (Processor p = new Date.Processor(false)) {
      testOne(
          p,
          "It was during mid-February 2015 that the event happened\"",
          "mid-February 2015",
          LocalDate.of(2015, 2, 11),
          LocalDate.of(2015, 2, 20));
    }
    try (Processor p = new Date.Processor(false)) {
      testOne(
          p,
          "It was during Late February 2015 that the event happened",
          "Late February 2015",
          LocalDate.of(2015, 2, 21),
          LocalDate.of(2015, 2, 28));
    }
    try (Processor p = new Date.Processor(false)) {
      testOne(
          p,
          "It was at the end of February 2015 that the event happened",
          "end of February 2015",
          LocalDate.of(2015, 2, 23),
          LocalDate.of(2015, 2, 28));
    }
    try (Processor p = new Date.Processor(false)) {
      testOne(
          p,
          "It was at the beginning of February 2015 that the event happened",
          "beginning of February 2015",
          LocalDate.of(2015, 2, 1),
          LocalDate.of(2015, 2, 5));
    }
    try (Processor p = new Date.Processor(false)) {
      testOne(
          p, "It was during Feb. 2015 that the event happened", "Feb. 2015", YearMonth.of(2015, 2));
    }
    try (Processor p = new Date.Processor(false)) {
      testOne(
          p,
          "It was during Late February 2015 that the event happened",
          "Late February 2015",
          LocalDate.of(2015, 2, 21),
          LocalDate.of(2015, 2, 28));
    }
  }

  @Test
  public void testYear() {

    try (Processor p = new Date.Processor(true)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "The year was 1997, which is the year after 1996 (a leap year). ABC1997, 1997ABC, and ABC1997ABC shouldn't be found!")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      Assertions.assertEquals(2, annotations.size());

      int i = 0;

      testAnnotationInstant(content, annotations.get(i++), "1997", Year.of(1997));

      testAnnotationInstant(content, annotations.get(i++), "1996", Year.of(1996));
    }
  }

  @Test
  public void testDecemberDate() {
    //Test to confirm that a bug has been fixed where December dates were mistakenly identified as American regardless of setting

    try (Processor p = new Date.Processor(false)) {
      Item item = new TestItem();

      Text content =
        item.createContent(TestStringContent.class)
          .withData("The date is 2/12/2020")
          .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
        store
          .getAll()
          .collect(Collectors.toList());

      Assertions.assertEquals(1, annotations.size());

      testAnnotationInstant(content, annotations.get(0), "2/12/2020", LocalDate.of(2020, Month.DECEMBER, 2));
    }

    // Now test it in American format
    try (Processor p = new Date.Processor(true)) {
      Item item = new TestItem();

      Text content =
        item.createContent(TestStringContent.class)
          .withData("The date is 2/12/2020")
          .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
        store
          .getAll()
          .collect(Collectors.toList());

      Assertions.assertEquals(1, annotations.size());

      testAnnotationInstant(content, annotations.get(0), "2/12/2020", LocalDate.of(2020, Month.FEBRUARY, 12));
    }
  }

  public void testOneRegex(String text, String covered) throws Exception {
    try (Processor p = new Date.Processor(false)) {

      Item item = new TestItem();

      Text content = item.createContent(TestStringContent.class).withData(text).save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations = store.getAll().collect(Collectors.toList());

      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);

      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals(covered, a.getBounds().getData(content).get());
    }
  }

  // The following tests were from the old Date regex, this shows we haven't lost capability with
  // the rewrite
  @Test
  public void testFull() throws Exception {
    testOneRegex("Today is Monday 25th February 2013.", "Monday 25th February 2013");
  }

  @Test
  public void testShortYear() throws Exception {
    testOneRegex("Today is Monday 25th February 13.", "Monday 25th February 13");
  }

  @Test
  public void testShortDay() throws Exception {
    testOneRegex("Today is Mon 25th February 2013.", "Mon 25th February 2013");
  }

  @Test
  public void testNoDay() throws Exception {
    testOneRegex("Today is 25th February 2013.", "25th February 2013");
  }

  @Test
  public void testNoSuffix() throws Exception {
    testOneRegex("Today is Monday 25 February 2013.", "Monday 25 February 2013");
  }

  @Test
  public void testInWord() throws Exception {
    testOneRegex("This is v2 Jul 2016.", "Jul 2016");
  }
}
