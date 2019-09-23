/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyTest {

  @Test
  public void testProperty() {
    Processor p = new Property.Processor("test", 123);

    Item item = new TestItem();

    assertFalse(item.getProperties().get("test").isPresent());

    p.process(item);

    assertTrue(item.getProperties().get("test").isPresent());
    assertEquals(123, item.getProperties().get("test").get());
  }
}
