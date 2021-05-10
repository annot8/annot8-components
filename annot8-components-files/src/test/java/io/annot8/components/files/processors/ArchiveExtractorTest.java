/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.TestItemFactory;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ArchiveExtractorTest {
  @Test
  public void testFile() {
    TestItem item = new TestItem();
    TestItemFactory itemFactory = (TestItemFactory) item.getItemFactory();

    item.createContent(FileContent.class).withData(getFile()).save();

    ArchiveExtractor.Processor p = new ArchiveExtractor.Processor(true);
    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(0, item.getContents().count());

    List<Item> createdItems = itemFactory.getCreatedItems();
    assertEquals(3, createdItems.size());

    assertTrue(
        createdItems.stream().allMatch(i -> i.getProperties().has(PropertyKeys.PROPERTY_KEY_NAME)));
    assertTrue(
        createdItems.stream()
            .allMatch(i -> i.getProperties().has(PropertyKeys.PROPERTY_KEY_SOURCE)));
  }

  @Test
  public void testInputstream() {
    TestItem item = new TestItem();
    TestItemFactory itemFactory = (TestItemFactory) item.getItemFactory();

    item.createContent(InputStreamContent.class)
        .withData(() -> ArchiveExtractorTest.class.getResourceAsStream("testArchive.zip"))
        .save();

    ArchiveExtractor.Processor p = new ArchiveExtractor.Processor(true);
    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(0, item.getContents().count());

    List<Item> createdItems = itemFactory.getCreatedItems();
    assertEquals(3, createdItems.size());

    assertTrue(
        createdItems.stream().allMatch(i -> i.getProperties().has(PropertyKeys.PROPERTY_KEY_NAME)));
    assertTrue(
        createdItems.stream()
            .noneMatch(i -> i.getProperties().has(PropertyKeys.PROPERTY_KEY_SOURCE)));
  }

  private File getFile() {
    URL resource = ArchiveExtractorTest.class.getResource("testArchive.zip");
    try {
      return Paths.get(resource.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
