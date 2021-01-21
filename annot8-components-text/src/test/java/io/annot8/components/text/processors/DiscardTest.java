/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class DiscardTest {
  @Test
  public void test() {
    Processor p = new Discard.Processor(Pattern.compile(".*bye.*"), false);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Hello, world!").save();
    item.createContent(TestStringContent.class).withData("Goodbye, world!").save();

    p.process(item);
    assertEquals(1L, item.getContents().count());

    assertEquals("Hello, world!", item.getContents(Text.class).findFirst().get().getData());
  }

  @Test
  public void testInverse() {
    Processor p = new Discard.Processor(Pattern.compile(".*bye.*"), true);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Hello, world!").save();
    item.createContent(TestStringContent.class).withData("Goodbye, world!").save();

    p.process(item);
    assertEquals(1L, item.getContents().count());

    assertEquals("Goodbye, world!", item.getContents(Text.class).findFirst().get().getData());
  }
}
