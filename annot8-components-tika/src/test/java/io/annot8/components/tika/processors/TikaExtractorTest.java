/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tika.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class TikaExtractorTest {

  @Test
  public void testInputStream() {
    Processor extractor = new TikaExtractor.Processor(false);

    Item item = new TestItem();
    item.createContent(InputStreamContent.class)
        .withData(TikaExtractorTest.class.getResourceAsStream("test.pdf"))
        .save();

    assertEquals(0, item.getContents(Text.class).count());
    assertEquals(1, item.getContents(InputStreamContent.class).count());

    extractor.process(item);

    assertEquals(
        1, item.getContents(Text.class).filter(c -> c.getData().contains("Hello world!")).count());
    assertEquals(1, item.getContents(InputStreamContent.class).count());
  }

  @Test
  public void testInputStreamRemoveSource() {
    Processor extractor = new TikaExtractor.Processor(true);

    Item item = new TestItem();
    item.createContent(InputStreamContent.class)
        .withData(TikaExtractorTest.class.getResourceAsStream("test.pdf"))
        .save();

    assertEquals(0, item.getContents(Text.class).count());
    assertEquals(1, item.getContents(InputStreamContent.class).count());

    extractor.process(item);

    assertEquals(
        1, item.getContents(Text.class).filter(c -> c.getData().contains("Hello world!")).count());
    assertEquals(0, item.getContents(InputStreamContent.class).count());
  }

  @Test
  public void testFile() throws IOException {
    Path p = Files.createTempFile("annot8-tika", ".txt");
    p.toFile().deleteOnExit();

    Files.write(p, "Hello world!".getBytes());

    Processor extractor = new TikaExtractor.Processor(false);

    Item item = new TestItem();
    item.createContent(FileContent.class).withData(p.toFile()).save();

    assertEquals(0, item.getContents(Text.class).count());
    assertEquals(1, item.getContents(FileContent.class).count());

    extractor.process(item);

    assertEquals(
        1, item.getContents(Text.class).filter(c -> c.getData().contains("Hello world!")).count());
    assertEquals(1, item.getContents(FileContent.class).count());
  }

  @Test
  public void testFileRemoveSource() throws IOException {
    Path p = Files.createTempFile("annot8-tika", ".txt");
    p.toFile().deleteOnExit();

    Files.write(p, "Hello world!".getBytes());

    Processor extractor = new TikaExtractor.Processor(true);

    Item item = new TestItem();
    item.createContent(FileContent.class).withData(p.toFile()).save();

    assertEquals(0, item.getContents(Text.class).count());
    assertEquals(1, item.getContents(FileContent.class).count());

    extractor.process(item);

    assertEquals(
        1, item.getContents(Text.class).filter(c -> c.getData().contains("Hello world!")).count());
    assertEquals(0, item.getContents(FileContent.class).count());
  }

  @Test
  public void testMissingFile() throws IOException {
    Processor extractor = new TikaExtractor.Processor(false);

    File f = new File("missing-file.txt");
    assertFalse(f.exists());

    Item item = new TestItem();
    item.createContent(FileContent.class).withData(f).save();

    assertEquals(0, item.getContents(Text.class).count());

    extractor.process(item);

    assertEquals(0, item.getContents(Text.class).count());
  }
}
