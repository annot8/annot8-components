/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.helpers.WithProperties;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class SplitTest {
  @Test
  public void testSplit() {
    Processor p = new Split.Processor(Pattern.compile("[aeiou]"), false, Collections.emptyMap());
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
    Processor p = new Split.Processor(Pattern.compile("[aeiou]"), true, Collections.emptyMap());
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Test").save();

    p.process(item);

    assertEquals(2L, item.getContents().count());

    List<String> l = item.getContents(Text.class).map(Text::getData).collect(Collectors.toList());
    assertTrue(l.contains("T"));
    assertTrue(l.contains("st"));

    item.getContents()
        .map(WithProperties::getProperties)
        .forEach(
            props -> {
              assertTrue(props.has(PropertyKeys.PROPERTY_KEY_INDEX));
              assertTrue(props.has(PropertyKeys.PROPERTY_KEY_PARENT));
            });
  }

  @Test
  public void testGroup() {
    Processor p =
        new Split.Processor(Pattern.compile("([aeiou])"), true, Map.of(1, "vowel", 2, "badGroup"));
    Item item = new TestItem();

    item.createContent(TestStringContent.class).withData("Testing").save();

    p.process(item);

    assertEquals(3L, item.getContents().count());

    List<String> l =
        item.getContents(Text.class)
            .map(t -> t.getData() + "__" + t.getProperties().getOrDefault("vowel", "NONE"))
            .collect(Collectors.toList());

    assertTrue(l.contains("T__NONE"));
    assertTrue(l.contains("st__e"));
    assertTrue(l.contains("ng__i"));
  }
}
