/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ExtractImageTest {
  @Test
  public void testInputStream() {
    InputStream is = ExtractImageTest.class.getClassLoader().getResourceAsStream("testimage.jpg");

    Item item = new TestItem();
    item.createContent(InputStreamContent.class).withData(() -> is).save();

    ExtractImage.Processor processor = new ExtractImage.Processor(new ExtractImage.Settings());
    ProcessorResponse pr = processor.process(item);

    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(1L, item.getContents(Image.class).count());
    assertNotNull(item.getContents(Image.class).findFirst().get().getData());
  }

  @Test
  public void testFile() throws URISyntaxException {
    File f = new File(ExtractImageTest.class.getClassLoader().getResource("testimage.jpg").toURI());

    Item item = new TestItem();
    item.createContent(FileContent.class).withData(() -> f).save();

    ExtractImage.Processor processor = new ExtractImage.Processor(new ExtractImage.Settings());
    ProcessorResponse pr = processor.process(item);

    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(1L, item.getContents(Image.class).count());
    assertNotNull(item.getContents(Image.class).findFirst().get().getData());
  }

  @Test
  public void testFileBadExtension() throws URISyntaxException {
    File f = new File(ExtractImageTest.class.getClassLoader().getResource("testimage.jpg").toURI());

    Item item = new TestItem();
    item.createContent(FileContent.class).withData(() -> f).save();

    ExtractImage.Settings settings = new ExtractImage.Settings();
    settings.setFileExtensions(List.of("png"));

    ExtractImage.Processor processor = new ExtractImage.Processor(settings);
    ProcessorResponse pr = processor.process(item);

    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(0L, item.getContents(Image.class).count());
  }

  @Test
  public void testDescriptor() {
    ExtractImage ei = new ExtractImage();
    ei.setSettings(new ExtractImage.Settings());

    assertNotNull(ei.capabilities());
    assertNotNull(ei.create(new SimpleContext()));
  }

  @Test
  public void testSettings() {
    ExtractImage.Settings s = new ExtractImage.Settings();
    assertTrue(s.validate());

    s.setDiscardOriginal(true);
    assertTrue(s.isDiscardOriginal());
    s.setDiscardOriginal(false);
    assertFalse(s.isDiscardOriginal());

    s.setFileExtensions(List.of("bmp"));
    assertEquals(List.of("bmp"), s.getFileExtensions());

    s.setFileExtensions(null);
    assertFalse(s.validate());
  }
}
