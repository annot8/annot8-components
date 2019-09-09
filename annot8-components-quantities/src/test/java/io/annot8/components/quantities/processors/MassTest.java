/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import org.junit.jupiter.api.Test;

import io.annot8.conventions.AnnotationTypes;

public class MassTest extends AbstractQuantityTest {

  public MassTest() {
    super(Mass.class, AnnotationTypes.ANNOTATION_TYPE_MASS, "kg");
  }

  @Test
  public void testKG() throws Exception {
    test("400kg of material was found.", "400kg", 400.0);
  }

  @Test
  public void testG() throws Exception {
    test("Mix in 30 grams of yellow powder.", "30 grams", 0.03);
  }

  @Test
  public void testMG() throws Exception {
    test("47 milligrams of powder is the correct amount.", "47 milligrams", 0.000047);
  }

  @Test
  public void testTonne() throws Exception {
    test("3.7 tonnes of explosive is enough to make a very big bang.", "3.7 tonnes", 3700.0);
  }

  @Test
  public void testTon() throws Exception {
    test("3.7 tons of explosive is enough to make a very big bang.", "3.7 tons", 3759.37356256);
  }

  @Test
  public void testLbs() throws Exception {
    test(
        "According to 3 processors, 4lb of explosive was carried across the border.",
        "4lb",
        1.81437);
  }

  @Test
  public void testStones() throws Exception {
    test("The brief case weighed 2 stone.", "2 stone", 12.70058636);
  }

  @Test
  public void testOunces() throws Exception {
    test("Add 4oz of sugar to the mix.", "4oz", 0.113398);
  }
}
