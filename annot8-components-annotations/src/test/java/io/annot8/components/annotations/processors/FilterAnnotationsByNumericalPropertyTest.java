/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

public class FilterAnnotationsByNumericalPropertyTest {
  @Test
  public void testDescriptor() {
    FilterAnnotationsByNumericalProperty.Settings settings =
        new FilterAnnotationsByNumericalProperty.Settings(
            "value", 1.0, FilterAnnotationsByNumericalProperty.Operator.GREATER_THAN, true);
    assertTrue(settings.validate());

    FilterAnnotationsByNumericalProperty d = new FilterAnnotationsByNumericalProperty();
    d.setSettings(settings);

    assertNotNull(d.capabilities());

    try (Processor p = d.createComponent(null, settings)) {
      assertNotNull(p);
    }
  }

  @Test
  public void testGreaterThan() {

    FilterAnnotationsByNumericalProperty.Settings settings =
        new FilterAnnotationsByNumericalProperty.Settings(
            "value", 1.0, FilterAnnotationsByNumericalProperty.Operator.GREATER_THAN, true);
    assertTrue(settings.validate());

    try (Processor p = new FilterAnnotationsByNumericalProperty.Processor(settings)) {

      Item item = createTestItem();
      ProcessorResponse pr = p.process(item);
      assertEquals(ProcessorResponse.ok(), pr);

      item.getContents()
          .forEach(
              c -> {
                assertEquals(1L, c.getAnnotations().getByType("double").count());
                assertEquals(0L, c.getAnnotations().getByType("integer").count());
                assertEquals(0L, c.getAnnotations().getByType("string").count());
                assertEquals(1L, c.getAnnotations().getByType("boolean").count());
                assertEquals(1L, c.getAnnotations().getByType("missing").count());
              });
    }
  }

  @Test
  public void testGreaterThanEquals() {

    FilterAnnotationsByNumericalProperty.Settings settings =
        new FilterAnnotationsByNumericalProperty.Settings(
            "value",
            1.0,
            FilterAnnotationsByNumericalProperty.Operator.GREATER_THAN_OR_EQUAL,
            true);
    assertTrue(settings.validate());

    try (Processor p = new FilterAnnotationsByNumericalProperty.Processor(settings)) {

      Item item = createTestItem();
      ProcessorResponse pr = p.process(item);
      assertEquals(ProcessorResponse.ok(), pr);

      item.getContents()
          .forEach(
              c -> {
                assertEquals(0L, c.getAnnotations().getByType("double").count());
                assertEquals(0L, c.getAnnotations().getByType("integer").count());
                assertEquals(0L, c.getAnnotations().getByType("string").count());
                assertEquals(1L, c.getAnnotations().getByType("boolean").count());
                assertEquals(1L, c.getAnnotations().getByType("missing").count());
              });
    }
  }

  @Test
  public void testLessThan() {

    FilterAnnotationsByNumericalProperty.Settings settings =
        new FilterAnnotationsByNumericalProperty.Settings(
            "value", 2.0, FilterAnnotationsByNumericalProperty.Operator.LESS_THAN, true);
    assertTrue(settings.validate());

    try (Processor p = new FilterAnnotationsByNumericalProperty.Processor(settings)) {

      Item item = createTestItem();
      ProcessorResponse pr = p.process(item);
      assertEquals(ProcessorResponse.ok(), pr);

      item.getContents()
          .forEach(
              c -> {
                assertEquals(0L, c.getAnnotations().getByType("double").count());
                assertEquals(1L, c.getAnnotations().getByType("integer").count());
                assertEquals(1L, c.getAnnotations().getByType("string").count());
                assertEquals(1L, c.getAnnotations().getByType("boolean").count());
                assertEquals(1L, c.getAnnotations().getByType("missing").count());
              });
    }
  }

  @Test
  public void testLessThanEquals() {

    FilterAnnotationsByNumericalProperty.Settings settings =
        new FilterAnnotationsByNumericalProperty.Settings(
            "value", 2.0, FilterAnnotationsByNumericalProperty.Operator.LESS_THAN_OR_EQUAL, true);
    assertTrue(settings.validate());

    try (Processor p = new FilterAnnotationsByNumericalProperty.Processor(settings)) {

      Item item = createTestItem();
      ProcessorResponse pr = p.process(item);
      assertEquals(ProcessorResponse.ok(), pr);

      item.getContents()
          .forEach(
              c -> {
                assertEquals(0L, c.getAnnotations().getByType("double").count());
                assertEquals(0L, c.getAnnotations().getByType("integer").count());
                assertEquals(1L, c.getAnnotations().getByType("string").count());
                assertEquals(1L, c.getAnnotations().getByType("boolean").count());
                assertEquals(1L, c.getAnnotations().getByType("missing").count());
              });
    }
  }

  @Test
  public void testEquals() {

    FilterAnnotationsByNumericalProperty.Settings settings =
        new FilterAnnotationsByNumericalProperty.Settings(
            "value", 2.0, FilterAnnotationsByNumericalProperty.Operator.EQUALS, true);
    assertTrue(settings.validate());

    try (Processor p = new FilterAnnotationsByNumericalProperty.Processor(settings)) {

      Item item = createTestItem();
      ProcessorResponse pr = p.process(item);
      assertEquals(ProcessorResponse.ok(), pr);

      item.getContents()
          .forEach(
              c -> {
                assertEquals(1L, c.getAnnotations().getByType("double").count());
                assertEquals(0L, c.getAnnotations().getByType("integer").count());
                assertEquals(1L, c.getAnnotations().getByType("string").count());
                assertEquals(1L, c.getAnnotations().getByType("boolean").count());
                assertEquals(1L, c.getAnnotations().getByType("missing").count());
              });
    }
  }

  @Test
  public void testNonNumeric() {

    FilterAnnotationsByNumericalProperty.Settings settings =
        new FilterAnnotationsByNumericalProperty.Settings(
            "value", 2.0, FilterAnnotationsByNumericalProperty.Operator.EQUALS, false);
    assertTrue(settings.validate());

    try (Processor p = new FilterAnnotationsByNumericalProperty.Processor(settings)) {

      Item item = createTestItem();
      ProcessorResponse pr = p.process(item);
      assertEquals(ProcessorResponse.ok(), pr);

      item.getContents()
          .forEach(
              c -> {
                assertEquals(1L, c.getAnnotations().getByType("double").count());
                assertEquals(0L, c.getAnnotations().getByType("integer").count());
                assertEquals(1L, c.getAnnotations().getByType("string").count());
                assertEquals(0L, c.getAnnotations().getByType("boolean").count());
                assertEquals(0L, c.getAnnotations().getByType("missing").count());
              });
    }
  }

  private Item createTestItem() {
    Item item = new TestItem();

    TestStringContent c1 = item.createContent(TestStringContent.class).withData("Test 1").save();
    c1.getAnnotations()
        .create()
        .withType("double")
        .withProperty("value", 1.0)
        .withBounds(ContentBounds.getInstance())
        .save();
    c1.getAnnotations()
        .create()
        .withType("integer")
        .withProperty("value", 2)
        .withBounds(ContentBounds.getInstance())
        .save();
    c1.getAnnotations()
        .create()
        .withType("string")
        .withProperty("value", "3.0")
        .withBounds(ContentBounds.getInstance())
        .save();
    c1.getAnnotations()
        .create()
        .withType("boolean")
        .withProperty("value", true)
        .withBounds(ContentBounds.getInstance())
        .save();
    c1.getAnnotations().create().withType("missing").withBounds(ContentBounds.getInstance()).save();

    return item;
  }
}
