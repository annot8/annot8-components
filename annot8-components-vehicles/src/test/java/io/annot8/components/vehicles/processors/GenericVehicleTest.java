/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.vehicles.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class GenericVehicleTest {
  @Test
  public void testCreation() {
    GenericVehicle genericVehicle = new GenericVehicle();
    Processor processor = genericVehicle.createComponent(null, NoSettings.getInstance());
    assertNotNull(processor);
    processor.close();
  }

  @Test
  public void testSimple() {
    testSingleMatch(
        "Natalie was seen driving an old rusty, blue boat.", "old rusty, blue boat", "maritime");
  }

  @Test
  public void testPlural() {
    testSingleMatch("Old Sam owned four cars.", "cars", "road");
  }

  @Test
  public void testNoDescriptor() {
    testSingleMatch("Sam owned a locomotive", "locomotive", "rail");
  }

  @Test
  public void testLand3() {
    testSingleMatch("Natalie owns a lime green van", "lime green van", "road");
  }

  @Test
  public void testVehicle() {
    testSingleMatch("4 unidentified vehicles were seen in the area.", "vehicles", null);
    testSingleMatch("The suspect's vehicle is red", "vehicle", null);
  }

  private void testSingleMatch(String sentence, String match, String subtype) {
    TestItem item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class).withData(sentence).save();

    Matcher m = Pattern.compile("\\w+").matcher(content.getData());
    while (m.find()) {
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(m.start(), m.end()))
          .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
          .save();
    }

    content
        .getAnnotations()
        .create()
        .withBounds(new SpanBounds(0, sentence.length()))
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .save();

    GenericVehicle genericVehicle = new GenericVehicle();
    Processor processor = genericVehicle.createComponent(null, NoSettings.getInstance());
    processor.process(item);

    assertEquals(
        1, content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_VEHICLE).count());

    Annotation a =
        content
            .getAnnotations()
            .getByType(AnnotationTypes.ANNOTATION_TYPE_VEHICLE)
            .findFirst()
            .get();
    assertNotNull(a);

    assertEquals(match, content.getText(a).get());

    if (subtype != null) {
      assertTrue(a.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
      assertEquals(
          subtype, a.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
    } else {
      assertFalse(a.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    }
  }
}
