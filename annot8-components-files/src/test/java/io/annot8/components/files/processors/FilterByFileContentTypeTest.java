/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.*;

import com.j256.simplemagic.ContentType;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FilterByFileContentTypeTest {
  @Test
  public void testFilterMatch() {
    FilterByFileContentType.Settings s = new FilterByFileContentType.Settings();
    s.setContentTypes(List.of(ContentType.ZIP));
    s.setFilterMatchingContentTypes(true);

    testFile(s, 0);
    testInputStream(s, 0);
  }

  @Test
  public void testNoFilterMatch() {
    FilterByFileContentType.Settings s = new FilterByFileContentType.Settings();
    s.setContentTypes(List.of(ContentType.ZIP));
    s.setFilterMatchingContentTypes(false);

    testFile(s, 1);
    testInputStream(s, 1);
  }

  @Test
  public void testNoFilterNoMatch() {
    FilterByFileContentType.Settings s = new FilterByFileContentType.Settings();
    s.setContentTypes(List.of(ContentType.JSON));
    s.setFilterMatchingContentTypes(false);

    testFile(s, 0);
    testInputStream(s, 0);
  }

  @Test
  public void testFilterNoMatch() {
    FilterByFileContentType.Settings s = new FilterByFileContentType.Settings();
    s.setContentTypes(List.of(ContentType.JSON));
    s.setFilterMatchingContentTypes(true);

    testFile(s, 1);
    testInputStream(s, 1);
  }

  private void testFile(FilterByFileContentType.Settings settings, long expected) {
    File f;
    try {
      f =
          Paths.get(FilterByFileContentTypeTest.class.getResource("testArchive.zip").toURI())
              .toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    Item item = new TestItem();
    item.createContent(FileContent.class).withData(f).save();

    FilterByFileContentType.Processor p = new FilterByFileContentType.Processor(settings);
    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(expected, item.getContents().count());
  }

  private void testInputStream(FilterByFileContentType.Settings settings, long expected) {
    Item item = new TestItem();
    item.createContent(InputStreamContent.class)
        .withData(FilterByFileContentTypeTest.class.getResourceAsStream("testArchive.zip"))
        .save();

    FilterByFileContentType.Processor p = new FilterByFileContentType.Processor(settings);
    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(expected, item.getContents().count());
  }

  @Test
  public void testFilterNullStream() {
    Item item = new TestItem();
    item.createContent(InputStreamContent.class)
        .withData(new ByteArrayInputStream(new byte[] {}))
        .save();

    FilterByFileContentType.Settings s = new FilterByFileContentType.Settings();
    s.setFilterNullContentTypes(true);

    FilterByFileContentType.Processor p = new FilterByFileContentType.Processor(s);
    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(0L, item.getContents().count());
  }

  @Test
  public void testFilterNullFile() throws IOException {
    File f = Files.createTempFile("contentypetest", "").toFile();
    Files.writeString(f.toPath(), "Test");
    f.deleteOnExit();

    Item item = new TestItem();
    item.createContent(FileContent.class).withData(f).save();

    FilterByFileContentType.Settings s = new FilterByFileContentType.Settings();
    s.setFilterNullContentTypes(true);

    FilterByFileContentType.Processor p = new FilterByFileContentType.Processor(s);
    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(0L, item.getContents().count());
  }

  @Test
  public void testDescriptor() {
    FilterByFileContentType d = new FilterByFileContentType();

    assertNotNull(d.capabilities());

    FilterByFileContentType.Processor p =
        d.createComponent(new SimpleContext(), new FilterByFileContentType.Settings());
    assertNotNull(p);
  }

  @Test
  public void testSettings() {
    FilterByFileContentType.Settings s = new FilterByFileContentType.Settings();

    assertFalse(s.validate());
    s.setContentTypes(List.of(ContentType.ZIP));
    assertTrue(s.validate());
  }
}
