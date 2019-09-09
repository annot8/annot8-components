/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import org.junit.jupiter.api.Test;

import io.annot8.conventions.AnnotationTypes;

public class AreaTest extends AbstractQuantityTest {

  public AreaTest() {
    super(Area.class, AnnotationTypes.ANNOTATION_TYPE_AREA, "m^2");
  }

  @Test
  public void testKM2() throws Exception {
    test("The region measured 800 square kilometres", "800 square kilometres", 8.0E8);
    test("The region measured 800 km^2", "800 km^2", 8.0E8);
  }

  @Test
  public void testM2() throws Exception {
    test("The field measured 400 square metres", "400 square metres", 400.0);
    test("The field measured 400 m^2", "400 m^2", 400.0);
  }

  @Test
  public void testCM2() throws Exception {
    test("The table measured 200 square centimetres", "200 square centimetres", 0.02);
    test("The table measured 200 cm^2", "200 cm^2", 0.02);
  }

  @Test
  public void testMM2() throws Exception {
    test("The chip measured 100 square millimetres", "100 square millimetres", 0.0001);
    test("The chip measured 100 mm^2", "100 mm^2", 0.0001);
  }

  @Test
  public void testMi2() throws Exception {
    test("The region measured 800 square miles", "800 square miles", 2.07199048E9);
    test("The region measured 800 mi^2", "800 mi^2", 2.07199048E9);
  }

  @Test
  public void testYd2() throws Exception {
    test("The field measured 400 square yards", "400 square yards", 334.450956);
    test("The field measured 400 yd^2", "400 yd^2", 334.450956);
  }

  @Test
  public void testFt2() throws Exception {
    test("The table measured 100 square feet", "100 square feet", 9.2903044);
    test("The table measured 100 ft^2", "100 ft^2", 9.2903044);
  }

  @Test
  public void testIn2() throws Exception {
    test("The chip measured 0.5 square inches", "0.5 square inches", 0.00032258);
    test("The chip measured 0.5 in^2", "0.5 in^2", 0.00032258);
  }

  @Test
  public void testAcre() throws Exception {
    test("The field measured 400 acres", "400 acres", 1618742.56);
  }

  @Test
  public void testHectare() throws Exception {
    test("The field measured 400 hectares", "400 hectares", 4000000.0);
  }
}
