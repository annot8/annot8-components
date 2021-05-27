/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.data.Item;
import io.annot8.api.properties.Properties;
import io.annot8.common.data.content.Image;
import io.annot8.testing.testimpl.TestItem;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

public class FlipRotateScaleImageTest {

  private void testQuads(
      BufferedImage image, Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
    assertEquals(topLeft, new Color(image.getRGB(0, 0)));
    assertEquals(topRight, new Color(image.getRGB(image.getWidth() - 1, 0)));
    assertEquals(bottomRight, new Color(image.getRGB(image.getWidth() - 1, image.getHeight() - 1)));
    assertEquals(bottomLeft, new Color(image.getRGB(0, image.getHeight() - 1)));
  }

  @Test
  public void testCopyProperties() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setCopyProperties(true);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .withProperty("en", "Hello World")
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);
    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    Properties props = item.getContents(Image.class).findFirst().orElseThrow().getProperties();
    assertTrue(props.has("en"));
  }

  @Test
  public void testDontCopyProperties() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setCopyProperties(false);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .withProperty("en", "Hello World")
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);
    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    Properties props = item.getContents(Image.class).findFirst().orElseThrow().getProperties();
    assertFalse(props.has("en"));
  }

  @Test
  public void testDontDiscard() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setDiscardOriginal(false);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);
    p.process(item);

    assertEquals(2, item.getContents(Image.class).count());
  }

  @Test
  public void testFlipHorizontal() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setFlipHorizontal(true);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    testQuads(img, Color.GREEN, Color.WHITE, Color.RED, Color.BLUE);
  }

  @Test
  public void testFlipVertical() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setFlipVertical(true);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    testQuads(img, Color.RED, Color.BLUE, Color.GREEN, Color.WHITE);
  }

  @Test
  public void testFlipHorizontalVertical() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setFlipHorizontal(true);
    s.setFlipVertical(true);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    testQuads(img, Color.BLUE, Color.RED, Color.WHITE, Color.GREEN);
  }

  @Test
  public void testRotate90() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setRotate(90.0);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    testQuads(img, Color.RED, Color.WHITE, Color.GREEN, Color.BLUE);
  }

  @Test
  public void testRotate180() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setFlipHorizontal(false);
    s.setFlipVertical(false);
    s.setRotate(180);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    testQuads(img, Color.BLUE, Color.RED, Color.WHITE, Color.GREEN);
  }

  @Test
  public void testScale2() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setScale(2.0);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    assertEquals(200, img.getWidth());
    assertEquals(200, img.getHeight());
  }

  @Test
  public void testScale2Clip() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setScale(2.0);
    s.setClipImage(true);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    assertEquals(100, img.getWidth());
    assertEquals(100, img.getHeight());
  }

  @Test
  public void testScale05() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setScale(0.5);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    assertEquals(50, img.getWidth());
    assertEquals(50, img.getHeight());
  }

  @Test
  public void testRotate30() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setRotate(30.0);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    assertEquals(136, img.getWidth());
    assertEquals(136, img.getHeight());

    assertEquals(Color.BLACK, new Color(img.getRGB(0, 0)));
    assertEquals(Color.WHITE, new Color(img.getRGB(50, 0)));
  }

  @Test
  public void testRotate60() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setRotate(60.0);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    assertEquals(136, img.getWidth());
    assertEquals(136, img.getHeight());

    assertEquals(Color.BLACK, new Color(img.getRGB(0, 0)));
    assertEquals(Color.WHITE, new Color(img.getRGB(86, 0)));
    assertEquals(Color.RED, new Color(img.getRGB(0, 50)));
  }

  @Test
  public void testRotateMinus45() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setRotate(-45.0);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    assertEquals(141, img.getWidth());
    assertEquals(141, img.getHeight());

    assertEquals(Color.BLACK, new Color(img.getRGB(0, 0)));
    assertEquals(Color.GREEN, new Color(img.getRGB(70, 0)));
    assertEquals(Color.WHITE, new Color(img.getRGB(0, 70)));
  }

  @Test
  public void testCombined() throws Exception {
    FlipRotateScaleImage.Settings s = new FlipRotateScaleImage.Settings();
    s.setScale(1.5);
    s.setFlipHorizontal(true);
    s.setRotate(20.0);
    s.setDiscardOriginal(true);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(
            ImageIO.read(
                FlipRotateScaleImageTest.class.getClassLoader().getResourceAsStream("quads.png")))
        .save();

    FlipRotateScaleImage.Processor p = new FlipRotateScaleImage.Processor(s);

    p.process(item);

    assertEquals(1, item.getContents(Image.class).count());

    BufferedImage img = item.getContents(Image.class).findFirst().orElseThrow().getData();
    assertEquals(192, img.getWidth());
    assertEquals(192, img.getHeight());

    int intersect = (int) (1.5 * Math.sin(Math.toRadians(20)) * 100);

    for (int x = 0; x < img.getWidth(); x++) {
      if (x == intersect || x == intersect + 1) {
        assertEquals(Color.GREEN, new Color(img.getRGB(x, 0)));
      } else {
        assertEquals(Color.BLACK, new Color(img.getRGB(x, 0)));
      }
    }
  }
}
