/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.easyocr.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class TestUtil {

  public static Item file() {
    try {
      Item item = new TestItem();
      item.createContent(FileContent.class)
          .withData(new File(TestUtil.class.getResource("test-image.tif").toURI()))
          .save();
      return item;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Item image() {
    try {
      Item item = new TestItem();
      BufferedImage img;
      try (InputStream is = TestUtil.class.getResourceAsStream("test-image.tif")) {
        img = ImageIO.read(is);
      }
      item.createContent(Image.class).withData(img).save();
      return item;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Item processFile(Processor ocr) {
    Item item = file();
    ocr.process(item);
    return item;
  }

  public static Item processImage(Processor ocr) {
    Item item = image();
    ocr.process(item);
    return item;
  }

  public static void assertText(Item item) {
    assertEquals(
        1,
        item.getContents(Text.class)
            .filter(t -> t.getData().trim().equals("Annot8 Test Image"))
            .count());
  }

  public static void checkCanProcessFile(Processor ocr) {
    assertText(processFile(ocr));
  }

  public static void checkCanProcessImage(Processor ocr) {
    assertText(processImage(ocr));
  }
}
