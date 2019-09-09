/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

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

public class CapitaliseTest {

  @Test
  public void testUpper() throws Annot8Exception {
    Capitalise capitalise = new Capitalise();
    Item item = new TestItem();

    Text lowerCase = item.create(TestStringContent.class).withName("test").withData("test").save();

    capitalise.process(item, lowerCase);

    List<String> collect = item.listNames().collect(Collectors.toList());

    assertTrue(item.hasContentOfName("test_upper"));
    List<Content<?>> capitalised = item.getContentByName("test_upper").collect(Collectors.toList());
    assertEquals(1, capitalised.size());
    assertEquals("TEST", capitalised.get(0).getData());
  }

  @Test
  public void testLower() throws Annot8Exception {

    Settings settings = new CapitaliseSettings(TextCase.LOWERCASE);
    Context context = new TestContext(settings);

    Capitalise capitalise = new Capitalise();
    capitalise.configure(context);

    Item item = new TestItem();

    Text upperCase = item.create(TestStringContent.class).withName("test").withData("TEST").save();

    capitalise.process(item, upperCase);

    List<String> collect = item.listNames().collect(Collectors.toList());

    assertTrue(item.hasContentOfName("test_lower"));
    List<Content<?>> capitalised = item.getContentByName("test_lower").collect(Collectors.toList());
    assertEquals(1, capitalised.size());
    assertEquals("test", capitalised.get(0).getData());
  }
}
