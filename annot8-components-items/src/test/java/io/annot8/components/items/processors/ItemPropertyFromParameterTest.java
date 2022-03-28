/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ItemPropertyFromParameterTest {

  private Item getProcessedItem(Processor p) {
    Item item = new TestItem();
    item.createContent(TestStringContent.class)
        .withData("This is a generic document text, to test a non-content annotator.")
        .withProperty("foo", "bar")
        .save();

    p.process(item);
    return item;
  }

  @Test
  public void testSetProperty() {
    try (Processor p = new ItemPropertyFromParameter.Processor("test", 42)) {
      Item item = getProcessedItem(p);

      Assertions.assertEquals(42, item.getProperties().get("test").get());
    }
  }

  @Test
  public void testRemoveProperty() {
    try (Processor p = new ItemPropertyFromParameter.Processor("foo", null)) {
      Item item = getProcessedItem(p);

      Assertions.assertFalse(item.getProperties().has("foo"));
    }
  }
}
