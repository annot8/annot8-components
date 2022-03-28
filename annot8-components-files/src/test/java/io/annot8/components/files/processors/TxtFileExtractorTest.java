/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.Processor;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TxtFileExtractorTest {

  @Test
  public void test() throws Exception {

    try (Processor p =
        new TxtFileExtractor.Processor(true, List.of("txt"), Charset.defaultCharset().name())) {

      TestItem item = new TestItem();

      URL resource = TxtFileExtractorTest.class.getResource("testfilemetadata.txt");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class).withDescription("Text file").withData(f).save();

      p.process(item);

      assertEquals(1L, item.getContents(Text.class).count());
      assertEquals(0L, item.getContents(FileContent.class).count());

      Text t = item.getContents(Text.class).findFirst().get();
      assertNotNull(t.getData());
      assertTrue(t.getData().length() > 0);
    }
  }

  @Test
  public void testNoMatch() throws Exception {

    try (Processor p =
        new TxtFileExtractor.Processor(true, List.of("text"), Charset.defaultCharset().name())) {

      TestItem item = new TestItem();

      URL resource = TxtFileExtractorTest.class.getResource("testfilemetadata.txt");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class).withDescription("Text file").withData(f).save();

      p.process(item);

      assertEquals(0L, item.getContents(Text.class).count());
      assertEquals(1L, item.getContents(FileContent.class).count());
    }
  }

  @Test
  public void testMatchAll() throws Exception {

    try (Processor p =
        new TxtFileExtractor.Processor(false, List.of(), Charset.defaultCharset().name())) {

      TestItem item = new TestItem();

      URL resource = TxtFileExtractorTest.class.getResource("testfilemetadata.txt");
      File f = Paths.get(resource.toURI()).toFile();

      item.createContent(FileContent.class).withDescription("Text file").withData(f).save();

      p.process(item);

      assertEquals(1L, item.getContents(Text.class).count());
      assertEquals(1L, item.getContents(FileContent.class).count());

      Text t = item.getContents(Text.class).findFirst().get();
      assertNotNull(t.getData());
      assertTrue(t.getData().length() > 0);
    }
  }
}
