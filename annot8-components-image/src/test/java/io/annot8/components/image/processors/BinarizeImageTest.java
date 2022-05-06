/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.properties.Properties;
import io.annot8.common.data.content.Image;
import io.annot8.components.image.processors.BinarizeImage.Method;
import io.annot8.testing.testimpl.TestItem;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BinarizeImageTest {

  private Item item;

  private void testQuads(
      BufferedImage image, Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
    assertEquals(topLeft, new Color(image.getRGB(0, 0)));
    assertEquals(topRight, new Color(image.getRGB(image.getWidth() - 1, 0)));
    assertEquals(bottomRight, new Color(image.getRGB(image.getWidth() - 1, image.getHeight() - 1)));
    assertEquals(bottomLeft, new Color(image.getRGB(0, image.getHeight() - 1)));
  }

  @BeforeEach
  public void setUp() throws IOException {
    item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(BinarizeImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .withProperty("en", "Hello World")
        .save();
  }

  @Test
  public void hasCapabilities() {
    BinarizeImage desc = new BinarizeImage();
    assertNotNull(desc.capabilities());
  }

  @Test
  public void testCopyPropertiesAndDiscards() throws Exception {
    BinarizeImage.Settings s =
        BinarizeImage.Settings.builder().withCopyProperties(true).withDiscardOriginal(true).build();

    try (Processor p = new BinarizeImage.Processor(s)) {
      p.process(item);

      assertEquals(1, item.getContents(Image.class).count());

      Properties props = item.getContents(Image.class).findFirst().orElseThrow().getProperties();
      assertTrue(props.has("en"));
    }
  }

  @Test
  public void testDontCopyPropertiesAndKeep() throws Exception {
    BinarizeImage.Settings s =
        BinarizeImage.Settings.builder()
            .withCopyProperties(false)
            .withDiscardOriginal(false)
            .build();

    BinarizeImage desc = new BinarizeImage();
    try (Processor p = desc.createComponent(null, s)) {
      p.process(item);

      assertEquals(2, item.getContents(Image.class).count());

      item.getContents(Image.class)
          .filter(i -> !i.getProperties().has("en"))
          .findFirst()
          .orElseThrow();
    }
  }

  @Test
  public void testLuminosity() throws Exception {
    BinarizeImage.Settings s =
        BinarizeImage.Settings.builder()
            .withDiscardOriginal(true)
            .withMethod(Method.LUMINOSITY)
            .build();

    try (BinarizeImage.Processor p = new BinarizeImage.Processor(s)) {

      p.process(item);

      assertEquals(1, item.getContents(Image.class).count());

      BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
      testQuads(img, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK);
    }
  }

  @Test
  public void testOtsu() throws Exception {
    BinarizeImage.Settings s =
        BinarizeImage.Settings.builder().withDiscardOriginal(true).withMethod(Method.OTSU).build();

    try (BinarizeImage.Processor p = new BinarizeImage.Processor(s)) {

      p.process(item);

      assertEquals(1, item.getContents(Image.class).count());

      BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
      testQuads(img, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK);
    }
  }
}
