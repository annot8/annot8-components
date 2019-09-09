/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import org.junit.jupiter.api.Test;

import io.annot8.conventions.AnnotationTypes;

public class DistanceTest extends AbstractQuantityTest {

  public DistanceTest() {
    super(Distance.class, AnnotationTypes.ANNOTATION_TYPE_DISTANCE, "m");
  }

  // Kilometres
  @Test
  public void testKm() throws Exception {
    test("It was 400 km North of London.", "400 km", 400000.0);
  }

  // Metres
  @Test
  public void testMetres() throws Exception {
    test("It was 800m North of London.", "800m", 800.0);
  }

  // Centimetres
  @Test
  public void testCentimetres() throws Exception {
    test("It was 50cm wide.", "50cm", 0.5);
  }

  // Millimetres
  @Test
  public void testMillimetres() throws Exception {
    test("It was 1mm thick wide.", "1mm", 0.001);
  }

  // Miles
  @Test
  public void testMiles() throws Exception {
    test("It was 1 mile wide.", "1 mile", 1609.344);
  }

  // Yards
  @Test
  public void testYards() throws Exception {
    test("It was 200 yards long.", "200 yards", 182.88);
  }

  // Inches
  @Test
  public void testInches() throws Exception {
    test("It was 60 inch deep.", "60 inch", 1.524);
  }

  // Inches
  @Test
  public void testNauticalMiles() throws Exception {
    test("It was 4 nautical miles wide.", "4 nautical miles", 7408.0);
  }
}
