/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

public class PrintSummaryTest {

  @Test
  public void testPrints() {

    TestPrintSettings settings = new TestPrintSettings();

    Item item = new TestItem();
    item.getProperties().set("en", "Hello World");
    Text text =
        item.createContent(Text.class)
            .withData("Hello World")
            .withProperty("en", "Hello World")
            .withProperty("fr", "Bonjour le monde")
            .save();

    text.getAnnotations()
        .create()
        .withType("ANNOTATION")
        .withProperty("fr", "Bonjour le monde")
        .save();
    text.getAnnotations().create().withType("ANNOTATION").withProperty("en", "Hello World").save();
    item.getGroups().create().withType("GROUP").withProperty("es", "Hola Mundo").save();

    PrintSummary.Processor processor = new PrintSummary.Processor(settings);
    processor.process(item);
    processor.close();

    String printed = settings.toString();
    assertTrue(printed.contains("Pipeline summary"));
    assertTrue(printed.contains("Processed 1 items and created:"));
    assertTrue(printed.contains("1 \tContent type\t TestStringContent"));
    assertTrue(printed.contains("2 \tAnnotation type\t ANNOTATION"));
    assertTrue(printed.contains("1 \tGroup type\t GROUP"));
    assertTrue(printed.contains("3 \tProperty type\t en"));
    assertTrue(printed.contains("2 \tProperty type\t fr"));
    assertTrue(printed.contains("1 \tProperty type\t es"));
  }

  @Test
  public void testPrintsProgress() {

    TestPrintSettings settings = new TestPrintSettings();
    settings.setReportProgress(1);

    Item item = new TestItem();
    item.getProperties().set("en", "Hello World");
    Text text =
        item.createContent(Text.class)
            .withData("Hello World")
            .withProperty("en", "Hello World")
            .withProperty("fr", "Bonjour le monde")
            .save();

    text.getAnnotations()
        .create()
        .withType("ANNOTATION")
        .withProperty("fr", "Bonjour le monde")
        .save();
    text.getAnnotations().create().withType("ANNOTATION").withProperty("en", "Hello World").save();
    item.getGroups().create().withType("GROUP").withProperty("es", "Hola Mundo").save();

    PrintSummary.Processor processor = new PrintSummary.Processor(settings);
    processor.process(item);

    String printed = settings.toString();

    assertTrue(printed.contains("Progress update"));
    assertTrue(printed.contains("Processed 1 items and created:"));
    assertTrue(printed.contains("1 \tContent type\t TestStringContent"));
    assertTrue(printed.contains("2 \tAnnotation type\t ANNOTATION"));
    assertTrue(printed.contains("1 \tGroup type\t GROUP"));
    assertTrue(printed.contains("3 \tProperty type\t en"));
    assertTrue(printed.contains("2 \tProperty type\t fr"));
    assertTrue(printed.contains("1 \tProperty type\t es"));

    processor.close();
  }
}
