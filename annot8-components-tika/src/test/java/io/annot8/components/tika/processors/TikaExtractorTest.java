/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tika.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.core.data.Item;
import io.annot8.testing.testimpl.TestItem;

public class TikaExtractorTest {

  @Test
  public void testInputStream() {
    TikaExtractor extractor = new TikaExtractor();

    Item item = new TestItem();
    item.create(InputStreamContent.class)
        .withName("test")
        .withData(TikaExtractorTest.class.getResourceAsStream("test.pdf"))
        .save();

    assertEquals(0, item.getContents(Text.class).count());

    extractor.process(item);

    assertEquals(
        1, item.getContents(Text.class).filter(c -> c.getData().contains("Hello world!")).count());
  }

  @Test
  public void testFile() throws IOException {
    Path p = Files.createTempFile("annot8-tika", ".txt");
    p.toFile().deleteOnExit();

    Files.write(p, "Hello world!".getBytes());

    TikaExtractor extractor = new TikaExtractor();

    Item item = new TestItem();
    item.create(FileContent.class).withName("test").withData(p.toFile()).save();

    assertEquals(0, item.getContents(Text.class).count());

    extractor.process(item);

    assertEquals(
        1, item.getContents(Text.class).filter(c -> c.getData().contains("Hello world!")).count());
  }

  @Test
  public void testMissingFile() throws IOException {
    TikaExtractor extractor = new TikaExtractor();

    File f = new File("missing-file.txt");
    assertFalse(f.exists());

    Item item = new TestItem();
    item.create(FileContent.class).withName("test").withData(f).save();

    assertEquals(0, item.getContents(Text.class).count());

    extractor.process(item);

    assertEquals(0, item.getContents(Text.class).count());
  }
}
