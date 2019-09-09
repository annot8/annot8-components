/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import org.junit.jupiter.api.Test;

import io.annot8.conventions.AnnotationTypes;

public class VolumeTest extends AbstractQuantityTest {

  public VolumeTest() {
    super(Volume.class, AnnotationTypes.ANNOTATION_TYPE_VOLUME, "m^3");
  }

  @Test
  public void testM3() throws Exception {
    test(
        "There was approximately 2 cubic metres of water in the container.", "2 cubic metres", 2.0);
  }

  @Test
  public void testCM3() throws Exception {
    test("They found 4.7cm^3 of blue sand", "4.7cm^3", 0.0000047);
    test("They found 4.7 cubic centimetres of yellow sand", "4.7 cubic centimetres", 0.0000047);
  }

  @Test
  public void testL() throws Exception {
    test("A 20 litre bucket was found hidden in the bushes.", "20 litre", 0.02);
    test("It contained 4.3l of petrol.", "4.3l", 0.0043);
  }

  @Test
  public void testML() throws Exception {
    test("A shot can be 25ml.", "25ml", 0.000025);
    test("Or a shot can be 35 millilitres", "35 millilitres", 0.000035);
  }

  @Test
  public void testPint() throws Exception {
    test("5.4 pints later, Tom had had enough to drink.", "5.4 pints", 0.0030672);
  }

  @Test
  public void testGallon() throws Exception {
    test("She filled the car up with 7 gallons of fuel.", "7 gallons", 0.03182263);
  }
}
