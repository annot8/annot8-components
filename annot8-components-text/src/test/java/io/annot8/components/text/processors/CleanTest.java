/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

public class CleanTest {

  private Clean.Settings getFalseSettings() {
    Clean.Settings s = new Clean.Settings();
    s.setRemoveSourceContent(true);
    s.setRemoveSingleNewLines(false);
    s.setTrim(false);
    s.setTrimLines(false);
    s.setReplaceSmartCharacters(false);
    s.setRemoveRepeatedWhitespace(false);

    return s;
  }

  @Test
  public void testSingleLines() {
    Clean.Settings s = getFalseSettings();
    s.setRemoveSingleNewLines(true);

    Processor p = new Clean.Processor(s);
    Item item = new TestItem();

    item.createContent(TestStringContent.class)
        .withData("Hello \r\nWorld!\n\n\n\nThis is an extreme-\nly long word.\n")
        .save();

    p.process(item);
    assertEquals(1L, item.getContents().count());

    assertEquals(
        "Hello World!\n\nThis is an extreme-ly long word.\n",
        item.getContents(Text.class).findFirst().get().getData());
  }

  @Test
  public void testTrim() {
    Clean.Settings s = getFalseSettings();
    s.setTrim(true);

    Processor p = new Clean.Processor(s);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData(" Hello \nworld!\t\n").save();

    p.process(item);
    assertEquals(1L, item.getContents().count());

    assertEquals("Hello \nworld!", item.getContents(Text.class).findFirst().get().getData());
  }

  @Test
  public void testTrimLines() {
    Clean.Settings s = getFalseSettings();
    s.setTrimLines(true);

    Processor p = new Clean.Processor(s);
    Item item = new TestItem();

    item.createContent(TestStringContent.class)
        .withData("Hello   \neveryone;\t \nHello \nworld!\t\n")
        .save();

    p.process(item);
    assertEquals(1L, item.getContents().count());

    assertEquals(
        "Hello\neveryone;\nHello\nworld!\n",
        item.getContents(Text.class).findFirst().get().getData());
  }

  @Test
  public void testSmartCharacters() {
    Clean.Settings s = getFalseSettings();
    s.setReplaceSmartCharacters(true);

    Processor p = new Clean.Processor(s);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("\u201cHello\u201d she said").save();

    p.process(item);
    assertEquals(1L, item.getContents().count());

    assertEquals("\"Hello\" she said", item.getContents(Text.class).findFirst().get().getData());
  }

  @Test
  public void testRepeatedWhitespace() {
    Clean.Settings s = getFalseSettings();
    s.setRemoveRepeatedWhitespace(true);

    Processor p = new Clean.Processor(s);
    Item item = new TestItem();

    item.createContent(TestStringContent.class)
        .withData("A  B \tC\t D\t\tE\t \tF \t G  \t \t  H")
        .save();

    p.process(item);
    assertEquals(1L, item.getContents().count());

    assertEquals("A B\tC\tD\tE\tF\tG\tH", item.getContents(Text.class).findFirst().get().getData());
  }
}
