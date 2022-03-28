/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.data.Item;
import io.annot8.common.data.content.Image;
import io.annot8.testing.testimpl.TestItem;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

public class FilterBlankImageTest {
  @Test
  public void test() throws IOException {
    BufferedImage testImage =
        ImageIO.read(
            FilterBlankImageTest.class.getClassLoader().getResourceAsStream("testimage.jpg"));
    BufferedImage blankRed =
        ImageIO.read(
            FilterBlankImageTest.class.getClassLoader().getResourceAsStream("blank_red.png"));
    BufferedImage blankTransparent =
        ImageIO.read(
            FilterBlankImageTest.class.getClassLoader().getResourceAsStream("blank_trans.png"));

    Item i = new TestItem();
    i.createContent(Image.class).withData(testImage).withDescription("Test Image").save();
    i.createContent(Image.class).withData(blankRed).withDescription("Blank Red").save();
    i.createContent(Image.class)
        .withData(blankTransparent)
        .withDescription("Blank Transparent")
        .save();

    try (FilterBlankImages.Processor p = new FilterBlankImages.Processor()) {
      p.process(i);

      List<Image> images = i.getContents(Image.class).collect(Collectors.toList());
      assertEquals(1, images.size());

      Image image = images.get(0);
      assertEquals("Test Image", image.getDescription());
    }
  }
}
