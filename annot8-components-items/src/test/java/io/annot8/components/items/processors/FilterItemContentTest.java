/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FilterItemContentTest {
  @Test
  public void testMatching() {
    Item item = new TestItem();
    item.createContent(Text.class).withData("Hello").save();
    item.createContent(Text.class).withData("Howdy").save();
    item.createContent(InputStreamContent.class).withData(InputStream.nullInputStream()).save();

    Processor fic = new FilterItemContent.Processor(List.of(Text.class.getName()), true);

    ProcessorResponse pr = fic.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(1L, item.getContents().count());
  }

  @Test
  public void testNonMatching() {
    Item item = new TestItem();
    item.createContent(Text.class).withData("Hello").save();
    item.createContent(Text.class).withData("Howdy").save();
    item.createContent(InputStreamContent.class).withData(InputStream.nullInputStream()).save();

    Processor fic = new FilterItemContent.Processor(List.of(Text.class.getName()), false);

    ProcessorResponse pr = fic.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(2L, item.getContents().count());
  }
}
