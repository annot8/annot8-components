/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

public class RenameItemPropertyTest {

  @Test
  public void testSettings() {
    RenameItemProperty.Settings s = new RenameItemProperty.Settings();

    assertFalse(s.validate());

    s.setExistingKey("foo");
    assertEquals("foo", s.getExistingKey());

    assertTrue(s.validate());

    s.setNewKey("bar");
    assertEquals("bar", s.getNewKey());

    assertTrue(s.validate());
  }

  @Test
  public void testRename() {
    TestItem item = new TestItem();
    item.getProperties().set("myFirstKey", 17);

    RenameItemProperty.Processor p = new RenameItemProperty.Processor("myFirstKey", "mySecondKey");
    p.process(item);

    assertFalse(item.getProperties().has("myFirstKey"));
    assertTrue(item.getProperties().has("mySecondKey"));

    assertEquals(17, item.getProperties().get("mySecondKey").get());
  }

  @Test
  public void testDelete() {
    TestItem item = new TestItem();
    item.getProperties().set("myFirstKey", 17);

    RenameItemProperty.Processor p = new RenameItemProperty.Processor("myFirstKey", null);
    p.process(item);

    assertFalse(item.getProperties().has("myFirstKey"));
  }

  @Test
  public void testRenameMissing() {
    TestItem item = new TestItem();
    item.getProperties().set("firstKey", 17);

    RenameItemProperty.Processor p = new RenameItemProperty.Processor("myFirstKey", "mySecondKey");
    p.process(item);

    assertTrue(item.getProperties().has("firstKey"));
  }
}
