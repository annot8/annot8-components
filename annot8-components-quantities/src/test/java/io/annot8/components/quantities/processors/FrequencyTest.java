/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import org.junit.jupiter.api.Test;

import io.annot8.conventions.AnnotationTypes;

public class FrequencyTest extends AbstractQuantityTest {

  public FrequencyTest() {
    super(Frequency.class, AnnotationTypes.ANNOTATION_TYPE_FREQUENCY, "Hz");
  }

  @Test
  public void testFrequency() throws Exception {
    test("Message received on 100 mHz", "100 mHz", 0.1);
    test("Message received on 5.7 Hz", "5.7 Hz", 5.7);
    test("Message received on 12kHz", "12kHz", 12000.0);
    test("Message received on 3.6MHz", "3.6MHz", 3.6E6);
    test("Message received on 5  GHz", "5  GHz", 5E9);
    test("Message received on 123.45THz", "123.45THz", 123.45E12);
  }
}
