/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.data.Item;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class RenderPdfTest {
  @Test
  public void testFile() throws Exception {
    RenderPdf.Processor p = new RenderPdf.Processor(300);
    Item i = new TestItem();

    URL uPdf = PdfExtractorTest.class.getResource("testDocument.pdf");
    File fPdf = Paths.get(uPdf.toURI()).toFile();

    i.createContent(FileContent.class).withData(fPdf).save();

    p.process(i);

    assertEquals(2, i.getContents(Image.class).count());
  }

  @Test
  public void testInputStream() {
    RenderPdf.Processor p = new RenderPdf.Processor(300);
    Item i = new TestItem();

    i.createContent(InputStreamContent.class)
        .withData(() -> PdfExtractorTest.class.getResourceAsStream("testDocument.pdf"))
        .save();

    p.process(i);

    assertEquals(2, i.getContents(Image.class).count());
  }

  @Test
  public void testSettings() {
    RenderPdf.Settings s = new RenderPdf.Settings();
    assertTrue(s.validate());

    s.setDpi(100);
    assertEquals(100, s.getDpi());

    s.setDpi(-10);
    assertFalse(s.validate());
  }

  @Test
  public void testDescriptor() {
    RenderPdf d = new RenderPdf();

    assertNotNull(d.capabilities());
    assertNotNull(d.createComponent(null, new RenderPdf.Settings()));
  }
}
