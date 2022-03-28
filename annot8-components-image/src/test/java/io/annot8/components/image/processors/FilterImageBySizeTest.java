/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Image;
import io.annot8.testing.testimpl.TestItem;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

public class FilterImageBySizeTest {
  @Test
  public void testSettings() {
    FilterImageBySize.Settings s = new FilterImageBySize.Settings();
    assertTrue(s.validate());

    s.setMinHeight(100);
    assertEquals(100, s.getMinHeight());

    s.setMinWidth(200);
    assertEquals(200, s.getMinWidth());

    s.setMaxHeight(300);
    assertEquals(300, s.getMaxHeight());

    s.setMaxWidth(400);
    assertEquals(400, s.getMaxWidth());
  }

  @Test
  public void testMinHeight() {
    Item i = new TestItem();

    BufferedImage bImg1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage bImg2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

    i.createContent(Image.class).withData(bImg1).save();
    Image img = i.createContent(Image.class).withData(bImg2).save();

    FilterImageBySize.Settings s = new FilterImageBySize.Settings();
    s.setMinHeight(150);

    try (FilterImageBySize.Processor p = new FilterImageBySize.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(i));

      assertEquals(1L, i.getContents(Image.class).count());
      assertEquals(img.getId(), i.getContents(Image.class).findFirst().get().getId());
    }
  }

  @Test
  public void testMinWidth() {
    Item i = new TestItem();

    BufferedImage bImg1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage bImg2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

    i.createContent(Image.class).withData(bImg1).save();
    Image img = i.createContent(Image.class).withData(bImg2).save();

    FilterImageBySize.Settings s = new FilterImageBySize.Settings();
    s.setMinWidth(150);

    try (FilterImageBySize.Processor p = new FilterImageBySize.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(i));

      assertEquals(1L, i.getContents(Image.class).count());
      assertEquals(img.getId(), i.getContents(Image.class).findFirst().get().getId());
    }
  }

  @Test
  public void testMaxHeight() {
    Item i = new TestItem();

    BufferedImage bImg1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage bImg2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

    Image img = i.createContent(Image.class).withData(bImg1).save();
    i.createContent(Image.class).withData(bImg2).save();

    FilterImageBySize.Settings s = new FilterImageBySize.Settings();
    s.setMaxHeight(150);

    try (FilterImageBySize.Processor p = new FilterImageBySize.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(i));

      assertEquals(1L, i.getContents(Image.class).count());
      assertEquals(img.getId(), i.getContents(Image.class).findFirst().get().getId());
    }
  }

  @Test
  public void testMaxWidth() {
    Item i = new TestItem();

    BufferedImage bImg1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    BufferedImage bImg2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

    Image img = i.createContent(Image.class).withData(bImg1).save();
    i.createContent(Image.class).withData(bImg2).save();

    FilterImageBySize.Settings s = new FilterImageBySize.Settings();
    s.setMaxWidth(150);

    try (FilterImageBySize.Processor p = new FilterImageBySize.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(i));

      assertEquals(1L, i.getContents(Image.class).count());
      assertEquals(img.getId(), i.getContents(Image.class).findFirst().get().getId());
    }
  }

  @Test
  public void testDescriptor() {
    FilterImageBySize filterImage = new FilterImageBySize();
    assertNotNull(filterImage.capabilities());
    assertNotNull(filterImage.createComponent(null, new FilterImageBySize.Settings()));
  }
}
