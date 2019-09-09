/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;

public class PropertyTest {

  @Test
  public void testProperty() throws Annot8Exception {
    Processor p = new Property();

    Property.PropertySettings ps = new Property.PropertySettings("test", 123);
    Context c = new TestContext(ps);
    p.configure(c);

    Item item = new TestItem();

    assertFalse(item.getProperties().get("test").isPresent());

    p.process(item);

    assertTrue(item.getProperties().get("test").isPresent());
    assertEquals(123, item.getProperties().get("test").get());
  }
}
