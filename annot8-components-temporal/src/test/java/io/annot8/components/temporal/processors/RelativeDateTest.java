/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.temporal.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.AnnotationCapability;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.capabilities.ContentCapability;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class RelativeDateTest {

  private static final LocalDate relativeTo = LocalDate.of(2016, 10, 5);
  private static final LocalDateTime relativeToTime = LocalDateTime.of(2016, 10, 5, 12, 45, 37);
  private static final ZonedDateTime relativeToTimeZone =
      ZonedDateTime.of(2016, 10, 5, 12, 45, 37, 0, ZoneOffset.UTC);

  @Test
  public void testRelativeDate() throws Exception {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    try (RelativeDate.Processor p =
        new RelativeDate.Processor(formatter, Arrays.asList("date", "documentDate"), false)) {
      Item item = new TestItem();

      item.getProperties().set("documentDate", relativeTo);
      assertEquals(relativeTo, p.fetchRelativeDate(item));

      item.getProperties().set("documentDate", relativeTo.format(formatter));

      assertEquals(relativeTo, p.fetchRelativeDate(item));

      item.getProperties().set("documentDate", relativeToTime);
      assertEquals(relativeTo, p.fetchRelativeDate(item));

      item.getProperties().set("documentDate", "banana");
      assertEquals(null, p.fetchRelativeDate(item));

      item.getProperties().set("documentDate", "10-05-1999");
      assertEquals(null, p.fetchRelativeDate(item));

      item.getProperties().set("documentDate", relativeToTimeZone);
      assertEquals(relativeTo, p.fetchRelativeDate(item));

      item.getProperties().set("documentDate", Integer.valueOf(7));
      assertEquals(null, p.fetchRelativeDate(item));
    }
  }

  public void testAnnotation(
      Text content,
      Annotation a,
      String coveredText,
      Temporal start,
      Temporal stop,
      boolean resolveDates) {
    assertEquals(content.getId(), a.getContentId());
    assertEquals(coveredText, a.getBounds().getData(content).get());
    if (resolveDates) {
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INTERVAL, a.getType());
      assertEquals(3, a.getProperties().getAll().size());
      assertEquals(start, a.getProperties().get(PropertyKeys.PROPERTY_KEY_START).get());
      assertEquals(stop, a.getProperties().get(PropertyKeys.PROPERTY_KEY_END).get());
      assertTrue(a.getProperties().has(PropertyKeys.PROPERTY_KEY_REFERENCE));
    } else {
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, a.getType());
      assertEquals(0, a.getProperties().getAll().size());
    }
  }

  public void testAnnotation(
      Text content, Annotation a, String coveredText, Temporal instant, boolean resolveDates) {
    assertEquals(content.getId(), a.getContentId());
    assertEquals(coveredText, a.getBounds().getData(content).get());
    if (resolveDates) {
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, a.getType());
      assertEquals(2, a.getProperties().getAll().size());
      assertEquals(instant, a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
      assertTrue(a.getProperties().has(PropertyKeys.PROPERTY_KEY_REFERENCE));
    } else {
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, a.getType());
      assertEquals(0, a.getProperties().getAll().size());
    }
  }

  @Test
  public void testToday() throws Exception {
    testToday(true);
    testToday(false);
  }

  public void testToday(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class).withData("Today is Wednesday").save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(1, annotations.size());

      testAnnotation(content, annotations.get(0), "Today", relativeTo, resolveDates);
    }
  }

  @Test
  public void testYesterday() throws Exception {
    testYesterday(true);
    testYesterday(false);
  }

  public void testYesterday(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Yesterday was Tuesday, and the day before yesterday was Monday")
              .withProperty("documentDate", relativeTo)
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content, annotations.get(i++), "Yesterday", relativeTo.minusDays(1), resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "day before yesterday",
          relativeTo.minusDays(2),
          resolveDates);
    }
  }

  @Test
  public void testTomorrow() throws Exception {
    testTomorrow(true);
    testTomorrow(false);
  }

  public void testTomorrow(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Tomorrow was Thursday, and the day after tomorrow is Friday")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content, annotations.get(i++), "Tomorrow", relativeTo.plusDays(1), resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "day after tomorrow",
          relativeTo.plusDays(2),
          resolveDates);
    }
  }

  @Test
  public void testThisX() throws Exception {
    testThisX(true);
    testThisX(false);
  }

  public void testThisX(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("This week is part of this month, which is part of this year.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(3, annotations.size());

      int i = 0;

      testAnnotation(
          content,
          annotations.get(i++),
          "This week",
          LocalDate.of(2016, 10, 3),
          LocalDate.of(2016, 10, 10),
          resolveDates);

      testAnnotation(
          content, annotations.get(i++), "this month", YearMonth.of(2016, 10), resolveDates);

      testAnnotation(content, annotations.get(i++), "this year", Year.of(2016), resolveDates);
    }
  }

  @Test
  public void testNextLastDay() throws Exception {
    testNextLastDay(true);
    testNextLastDay(false);
  }

  public void testNextLastDay(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Next Friday is in two days time. Last Wednesday was seven days ago.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content, annotations.get(i++), "Next Friday", relativeTo.plusDays(2), resolveDates);

      testAnnotation(
          content, annotations.get(i++), "Last Wednesday", relativeTo.minusDays(7), resolveDates);
    }
  }

  @Test
  public void testNextLastWeek() throws Exception {
    testNextLastWeek(true);
    testNextLastWeek(false);
  }

  public void testNextLastWeek(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Next week begins on the 10th October, last week began on 26th September.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content,
          annotations.get(i++),
          "Next week",
          LocalDate.of(2016, 10, 10),
          LocalDate.of(2016, 10, 17),
          resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "last week",
          LocalDate.of(2016, 9, 26),
          LocalDate.of(2016, 10, 3),
          resolveDates);
    }
  }

  @Test
  public void testNextLastWeekPeriod() throws Exception {
    testNextLastWeekPeriod(true);
    testNextLastWeekPeriod(false);
  }

  public void testNextLastWeekPeriod(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "In the next week we expect to see results from what happened within the last week.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content,
          annotations.get(i++),
          "In the next week",
          relativeTo,
          relativeTo.plusDays(7),
          resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "within the last week",
          relativeTo.minusDays(7),
          relativeTo,
          resolveDates);
    }
  }

  @Test
  public void testNextLastWeekDay() throws Exception {
    testNextLastWeekDay(true);
    testNextLastWeekDay(false);
  }

  public void testNextLastWeekDay(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Tuesday next week we hope to do better than we did on Thursday last week.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content,
          annotations.get(i++),
          "Tuesday next week",
          LocalDate.of(2016, 10, 11),
          resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "Thursday last week",
          LocalDate.of(2016, 9, 29),
          resolveDates);
    }
  }

  @Test
  public void testNextLastMonth() throws Exception {
    testNextLastMonth(true);
    testNextLastMonth(false);
  }

  public void testNextLastMonth(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Last month was September, and next month is November")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content, annotations.get(i++), "Last month", YearMonth.of(2016, 9), resolveDates);

      testAnnotation(
          content, annotations.get(i++), "next month", YearMonth.of(2016, 11), resolveDates);
    }
  }

  @Test
  public void testNextLastMonthPeriod() throws Exception {
    testNextLastMonthPeriod(true);
    testNextLastMonthPeriod(false);
  }

  public void testNextLastMonthPeriod(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "In the last month something happened, but it's not expected to happen again within the next month")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content,
          annotations.get(i++),
          "In the last month",
          relativeTo.minusMonths(1),
          relativeTo,
          resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "within the next month",
          relativeTo,
          relativeTo.plusMonths(1),
          resolveDates);
    }
  }

  @Test
  public void testNextLastYear() throws Exception {
    testNextLastYear(true);
    testNextLastYear(false);
  }

  public void testNextLastYear(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Next year is 2017, last year was 2016.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(content, annotations.get(i++), "Next year", Year.of(2017), resolveDates);

      testAnnotation(content, annotations.get(i++), "last year", Year.of(2015), resolveDates);
    }
  }

  @Test
  public void testNextLastYearPeriod() throws Exception {
    testNextLastYearPeriod(true);
    testNextLastYearPeriod(false);
  }

  public void testNextLastYearPeriod(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "In the next year, something will happen which didn't happen within the last year.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content,
          annotations.get(i++),
          "In the next year",
          relativeTo,
          relativeTo.plusYears(1),
          resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "within the last year",
          relativeTo.minusYears(1),
          relativeTo,
          resolveDates);
    }
  }

  @Test
  public void testNextLastYearMonth() throws Exception {
    testNextLastYearMonth(true);
    testNextLastYearMonth(false);
  }

  public void testNextLastYearMonth(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("October last year was cold, but June next year will probably be hot.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(2, annotations.size());

      int i = 0;

      testAnnotation(
          content, annotations.get(i++), "October last year", YearMonth.of(2015, 10), resolveDates);

      testAnnotation(
          content, annotations.get(i++), "June next year", YearMonth.of(2017, 6), resolveDates);
    }
  }

  @Test
  public void testInTheNextX() throws Exception {
    testInTheNextX(true);
    testInTheNextX(false);
  }

  public void testInTheNextX(boolean resolveDates) throws Exception {
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(
            DateTimeFormatter.ofPattern("yyyy-mm-dd"),
            Arrays.asList("date", "documentDate"),
            false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "It could happen in the next 3 days, or within the next 2 weeks. Or it could have happened in the last 4 months or within the last 15 years.")
              .save();

      if (resolveDates) item.getProperties().set("documentDate", relativeTo);

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(4, annotations.size());

      int i = 0;

      testAnnotation(
          content,
          annotations.get(i++),
          "in the next 3 days",
          relativeTo,
          relativeTo.plusDays(3),
          resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "within the next 2 weeks",
          relativeTo,
          relativeTo.plusWeeks(2),
          resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "in the last 4 months",
          relativeTo.minusMonths(4),
          relativeTo,
          resolveDates);

      testAnnotation(
          content,
          annotations.get(i++),
          "within the last 15 years",
          relativeTo.minusYears(15),
          relativeTo,
          resolveDates);
    }
  }

  @Test
  public void testMetadataCase() throws Exception {

    DateTimeFormatter formatter = createDTF("d MMM yy");
    try (RelativeDate.Processor p =
        new RelativeDate.Processor(formatter, Arrays.asList("date", "documentDate"), false)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("This week is the end of term.")
              .save();

      item.getProperties().set("date", "15 MAY 17");
      assertEquals(LocalDate.of(2017, 5, 15), p.fetchRelativeDate(item));

      p.process(item);

      AnnotationStore store = content.getAnnotations();
      List<Annotation> annotations =
          store
              .getAll()
              .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      assertEquals(1, annotations.size());

      int i = 0;

      testAnnotation(
          content,
          annotations.get(i++),
          "This week",
          LocalDate.of(2017, 5, 15),
          LocalDate.of(2017, 5, 22),
          true);
    }
  }

  private DateTimeFormatter createDTF(String pattern) {
    return new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern(pattern)
        .toFormatter();
  }

  @Test
  public void testCapabilities() {
    RelativeDate n = new RelativeDate();

    // Get the capabilities and check that we have the expected number
    Capabilities c = n.capabilities();
    assertEquals(3, c.creates().count());
    assertEquals(1, c.processes().count());
    assertEquals(0, c.deletes().count());

    // Check that we're creating an Annotation and that it has the correct definitions
    assertTrue(
        c.creates(AnnotationCapability.class)
            .allMatch(cap -> cap.getType().startsWith("entity/temporal")));

    // Check that we're processing a Content and that it has the correct definitions
    ContentCapability contentCap = c.processes(ContentCapability.class).findFirst().get();
    assertEquals(Text.class, ((ContentCapability) contentCap).getType());
  }

  @Test
  public void testCreateComponent() {
    RelativeDate n = new RelativeDate();

    // Test that we actually get a component when we create it
    RelativeDate.Processor np =
        n.createComponent(
            null,
            new RelativeDate.Settings("yyyy-mm-dd", Arrays.asList("date", "documentDate"), false));
    assertNotNull(np);
  }
}
