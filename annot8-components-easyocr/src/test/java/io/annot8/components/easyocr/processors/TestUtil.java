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
import java.io.InputStream;
import javax.imageio.ImageIO;

public class TestUtil {

  public static void checkCanProcessFile(Processor ocr) throws Exception {
    Item item = new TestItem();
    item.createContent(FileContent.class)
        .withData(new File(TestUtil.class.getResource("test-image.tif").toURI()))
        .save();

    assertEquals(0, item.getContents(Text.class).count());

    ocr.process(item);

    assertEquals(
        1,
        item.getContents(Text.class)
            .filter(t -> t.getData().trim().equals("Annot8 Test Image"))
            .count());
  }

  public static void checkCanProcessImage(Processor ocr) throws Exception {
    Item item = new TestItem();

    BufferedImage img;
    try (InputStream is = TestUtil.class.getResourceAsStream("test-image.tif")) {
      img = ImageIO.read(is);
    }

    Image content1 = item.createContent(Image.class).withData(img).save();

    assertEquals(0, item.getContents(Text.class).count());

    ocr.process(item);

    assertEquals(
        1,
        item.getContents(Text.class)
            .filter(t -> t.getData().trim().equals("Annot8 Test Image"))
            .count());
  }
}
