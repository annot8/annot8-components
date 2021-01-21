/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class SplitTest {
  @Test
  public void testSplit() {
    Processor p = new Split.Processor(Pattern.compile("[aeiou]"), false);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Test").save();

    p.process(item);

    assertEquals(3L, item.getContents().count());

    List<String> l = item.getContents(Text.class).map(Text::getData).collect(Collectors.toList());
    assertTrue(l.contains("Test"));
    assertTrue(l.contains("T"));
    assertTrue(l.contains("st"));
  }

  @Test
  public void testRemove() {
    Processor p = new Split.Processor(Pattern.compile("[aeiou]"), true);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Test").save();

    p.process(item);

    assertEquals(2L, item.getContents().count());

    List<String> l = item.getContents(Text.class).map(Text::getData).collect(Collectors.toList());
    assertTrue(l.contains("T"));
    assertTrue(l.contains("st"));
  }
}
