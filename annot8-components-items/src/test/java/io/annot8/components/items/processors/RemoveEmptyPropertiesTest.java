/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.data.Item;
import io.annot8.testing.testimpl.TestItem;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class RemoveEmptyPropertiesTest {
  @Test
  public void test() {
    Item item = new TestItem();
    item.getProperties().set("nullProperty", null);
    item.getProperties().set("emptyStringProperty", "");
    item.getProperties().set("blankStringProperty", " ");
    item.getProperties().set("blankStringProperty2", "\t");
    item.getProperties().set("stringProperty", "Hello World!");
    item.getProperties().set("intProperty", 123);

    try (RemoveEmptyProperties.Processor p = new RemoveEmptyProperties.Processor()) {
      p.process(item);

      List<String> keys = item.getProperties().keys().collect(Collectors.toList());
      assertEquals(2, keys.size());
      assertTrue(keys.contains("stringProperty"));
      assertTrue(keys.contains("intProperty"));
    }
  }

  @Test
  public void testCapabilities() {
    RemoveEmptyProperties d = new RemoveEmptyProperties();
    assertNotNull(d.capabilities());
  }
}
