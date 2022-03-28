/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class TextToFileTest {

  @Test
  public void test() throws IOException {
    Item item = new TestItem();
    Text text = item.createContent(Text.class).withData("Hello, World!").save();

    Path tempFolder = Files.createTempDirectory("test");
    TextToFile.Settings s = new TextToFile.Settings();
    s.setOutputFolder(tempFolder);

    try (TextToFile.Processor p = new TextToFile.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(item));

      Path itemFolder = tempFolder.resolve(item.getId());
      Path contentFile = itemFolder.resolve(text.getId() + ".txt");

      String content = Files.readString(contentFile);
      assertEquals("Hello, World!", content);

      contentFile.toFile().delete();
      itemFolder.toFile().delete();
      tempFolder.toFile().delete();
    }
  }
}
