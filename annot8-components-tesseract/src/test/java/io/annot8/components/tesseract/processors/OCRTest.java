/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tesseract.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import io.annot8.core.data.Item;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;

public class OCRTest {

  @Test
  @Disabled // Requires Tesseract to be correctly installed
  public void test() throws Exception {
    OCR ocr = new OCR();
    ocr.configure(new TestContext());

    Item item = new TestItem();
    item.create(FileContent.class)
        .withName("test")
        .withData(new File(OCRTest.class.getResource("test-image.tif").toURI()))
        .save();

    assertEquals(0, item.getContents(Text.class).count());

    ocr.process(item);
    assertEquals(
        1,
        item.getContents(Text.class)
            .filter(t -> t.getData().trim().equals("Annot8 Test Image"))
            .count());
  }
}
