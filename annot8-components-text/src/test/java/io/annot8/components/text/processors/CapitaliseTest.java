/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.Text;
import io.annot8.components.text.processors.Capitalise.CapitaliseSettings;
import io.annot8.components.text.processors.Capitalise.TextCase;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.settings.Settings;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class CapitaliseTest {

  @Test
  public void testUpper() {
    Capitalise capitalise = new Capitalise();
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Test").save();

    capitalise.process(item);

    assertThat(item.getContents(Text.class).map(Text::getData)).anyMatch(s -> s.equals("TEST"));
  }

  @Test
  public void testLower() {

    Settings settings = new CapitaliseSettings(TextCase.LOWERCASE);
    Context context = new TestContext(settings);

    Capitalise capitalise = new Capitalise();
    capitalise.configure(context);

    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Test").save();

    capitalise.process(item);

    assertThat(item.getContents(Text.class).map(Text::getData)).anyMatch(s -> s.equals("test"));
  }
}
