/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static com.google.common.base.CharMatcher.javaLowerCase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.Text;
import io.annot8.components.text.processors.Capitalise.CapitaliseSettings;
import io.annot8.components.text.processors.Capitalise.TextCase;
import io.annot8.core.context.Context;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.settings.Settings;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.platform.commons.util.StringUtils;

public class CapitaliseTest {

  @Test
  public void testUpper() {
    Capitalise capitalise = new Capitalise();
    Item item = new TestItem();

    item.create(TestStringContent.class).withData("Test").save();

    capitalise.process(item);

    assertThat(item.getContents(Text.class).map(Text::getData))
            .anyMatch(s -> s.equals("TEST"));
  }

  @Test
  public void testLower() {

    Settings settings = new CapitaliseSettings(TextCase.LOWERCASE);
    Context context = new TestContext(settings);

    Capitalise capitalise = new Capitalise();
    capitalise.configure(context);

    Item item = new TestItem();

    item.create(TestStringContent.class).withData("Test").save();

    capitalise.process(item);

    assertThat(item.getContents(Text.class)
            .map(Text::getData))
            .anyMatch(s -> s.equals("test"));
  }
}
