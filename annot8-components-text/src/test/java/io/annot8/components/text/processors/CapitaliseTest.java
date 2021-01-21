/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.assertj.core.api.Assertions.assertThat;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.components.text.processors.Capitalise.TextCase;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

public class CapitaliseTest {

  @Test
  public void testUpper() {
    Processor capitalise = new Capitalise.Processor(TextCase.UPPERCASE, false);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Test").save();

    capitalise.process(item);

    assertThat(item.getContents(Text.class).map(Text::getData)).anyMatch(s -> s.equals("TEST"));
  }

  @Test
  public void testLower() {
    Processor capitalise = new Capitalise.Processor(TextCase.LOWERCASE, false);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Test").save();

    capitalise.process(item);

    assertThat(item.getContents(Text.class).map(Text::getData)).anyMatch(s -> s.equals("test"));
  }

  @Test
  public void testRemove() {
    Processor capitalise = new Capitalise.Processor(TextCase.UPPERCASE, true);
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Test").save();

    capitalise.process(item);

    assertThat(item.getContents(Text.class).map(Text::getData)).allMatch(s -> s.equals("TEST"));
  }
}
