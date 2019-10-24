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
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeTest {

  @Test
  public void testCapabilities() {
    Time n = new Time();

    // Get the capabilities and check that we have the expected number
    Capabilities c = n.capabilities();
    assertEquals(1, c.creates().count());
    assertEquals(1, c.processes().count());
    assertEquals(0, c.deletes().count());

    // Check that we're creating an Annotation and that it has the correct definitions
    AnnotationCapability annotCap = c.creates(AnnotationCapability.class).findFirst().get();
    assertEquals(SpanBounds.class, ((AnnotationCapability) annotCap).getBounds());
    assertEquals(
        AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, ((AnnotationCapability) annotCap).getType());

    // Check that we're processing a Content and that it has the correct definitions
    ContentCapability contentCap = c.processes(ContentCapability.class).findFirst().get();
    assertEquals(Text.class, ((ContentCapability) contentCap).getType());
  }

  @Test
  public void testCreateComponent() {
    Time n = new Time();

    // Test that we actually get a component when we create it
    Time.Processor np = n.createComponent(null, new Time.Settings());
    assertNotNull(np);
  }

  public void timeRegexCountAndValueCheck(
      Processor p, String text, Integer expectedCount, String... expectedValues) {
    Item item = new TestItem();

    //    Item item = new SimpleItem(itemFactory, contentBuilderFactoryRegistry);
    Text content = item.createContent(TestStringContent.class).withData(text).save();

    p.process(item);

    AnnotationStore store = content.getAnnotations();
    List<Annotation> annotations =
        store
            .getAll()
            .sorted(Comparator.comparingInt(o -> o.getBounds(SpanBounds.class).get().getBegin()))
            .collect(Collectors.toList());

    Assertions.assertEquals(expectedCount, annotations.size());

    for (int i = 0; i < expectedValues.length; i++) {
      Annotation a = annotations.get(i);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals(expectedValues[i], a.getBounds().getData(content).get());
      Assertions.assertEquals(0, a.getProperties().getAll().size());
    }
  }

  // Catch a single reference in 12hr clock format - morning
  @Test
  public void testSingleTime12hrClockMorning() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "It is currently 07:00.", 1, "07:00");
    }
  }

  // Catch a single reference in 12hr clock format - evening
  @Test
  public void testSingleTime12hrClockEvening() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "It is currently 20:00.", 1, "20:00");
    }
  }

  // Catch the am/pm qualifier
  @Test
  public void testSingleTimeWithAMQualifier() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "It is currently 12:22pm", 1, "12:22pm");
    }
  }

  // Catch informal time references: midnight, noon, midday.
  @Test
  public void testSingleTimeInformal() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(
          p,
          "I don't know whether its midnight, noon or midday today.",
          3,
          "midnight",
          "noon",
          "midday");
    }
  }

  // Catch a single time with a timezone qualifier
  @Test
  public void testSingleGmtTimeZone() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "It is currently 11:00 GMT", 1, "11:00 GMT");
    }
  }

  // Catch Central Europe Time zone
  @Test
  public void testSingleGmtOtherTimeZone() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "It is currently 11:00 CET", 1, "11:00 CET");
    }
  }

  // Ensure we catch multiple time references in 1 document
  @Test
  public void testMultipleTimeReferences() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(
          p,
          "It was close to noon, about 12:03pm, 8 hours before 20:00.",
          3,
          "noon",
          "12:03pm",
          "20:00");
    }
  }

  // Catch a single reference in 24hr clock
  @Test
  public void testSingleTime24hrClock() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "It is currently 1700hrs.", 1, "1700hrs");
    }
  }

  // Catch a single reference in 24hr clock
  @Test
  public void testSingleTime24hrClockTimeZone() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "It is currently 1700hrs CET.", 1, "1700hrs CET");
    }
  }
  // Catch a reference containing a colon
  @Test
  public void testYearAndTimeColon() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "The year was 2016, the time was 20:16", 1, "20:16");
    }
  }

  // Catch a reference suffixed with hrs
  @Test
  public void testYearAndTimeHrs() throws Exception {
    try (Processor p = new Time.Processor(true)) {
      timeRegexCountAndValueCheck(p, "The year was 2016, the time was 2016hrs", 1, "2016hrs");
    }
  }

  // Test requireAlpha = false
  @Test
  public void testAlphaOnlyFalse() throws Exception {
    try (Processor p = new Time.Processor(false)) {
      timeRegexCountAndValueCheck(
          p, "The year was 2016, the time was 2016hrs", 2, "2016", "2016hrs");
    }
  }
}
