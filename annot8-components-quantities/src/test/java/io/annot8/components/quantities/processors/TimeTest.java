/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.annot8.api.capabilities.AnnotationCapability;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.capabilities.ContentCapability;
import io.annot8.api.components.Processor;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import org.junit.jupiter.api.Test;

public class TimeTest extends AbstractQuantityTest {

  public TimeTest() {
    super(Time.class, AnnotationTypes.ANNOTATION_TYPE_QUANTITY, "s");
  }

  @Test
  public void testYear() throws Exception {
    test("6 years later", "6 years", 189216000d);
  }

  @Test
  public void testMonth() throws Exception {

    test("In 18 months", "18 months", 47304000d);
  }

  @Test
  public void testWeek() throws Exception {

    test("In the next 2 weeks", "2 weeks", 1209600d);
  }

  @Test
  public void testdays() throws Exception {

    test("460 days after the event", "460 days", 39744000d);
  }

  @Test
  public void testHours() throws Exception {

    test("Only 2 hours to go...", "2 hours", 7200d);
  }

  @Test
  public void testHoursTime() throws Exception {

    testCount("At 2200hrs, things will happen... But they'll be over by 0200hrs", 0);
  }

  @Test
  public void testMinutes() throws Exception {

    test("27 minutes until it happens", "27 minutes", 1620d);
  }

  @Test
  public void testSeconds() throws Exception {

    test("In 30 seconds time", "30 seconds", 30d);
  }

  @Test
  public void testPunctuation() throws Exception {
    test("There are 86,400 seconds in a day.", "86,400 seconds", 86400d);
  }

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
        AnnotationTypes.ANNOTATION_TYPE_QUANTITY, ((AnnotationCapability) annotCap).getType());

    // Check that we're processing a Content and that it has the correct definitions
    ContentCapability contentCap = c.processes(ContentCapability.class).findFirst().get();
    assertEquals(Text.class, ((ContentCapability) contentCap).getType());
  }

  @Test
  public void testCreateComponent() {
    Time n = new Time();

    // Test that we actually get a component when we create it
    Processor np = n.createComponent(null, NoSettings.getInstance());
    assertNotNull(np);
  }
}
