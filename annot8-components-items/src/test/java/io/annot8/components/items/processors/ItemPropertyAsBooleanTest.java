/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

public class ItemPropertyAsBooleanTest {
  @Test
  public void test() {
    test(false, false);
    test("false", false);
    test("foo bar", false);
    test(123, false);

    test(true, true);
    test(" TRUE ", true);
    test("yes", true);
  }

  private void test(Object orig, Boolean expected) {
    Item item = new TestItem();
    item.getProperties().set("val", orig);

    ItemPropertyAsBoolean.Settings s = new ItemPropertyAsBoolean.Settings();
    s.setKey("val");

    try (ItemPropertyAsBoolean.Processor p = new ItemPropertyAsBoolean.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(item));

      assertTrue(item.getProperties().has("val"));
      assertEquals(Boolean.class, item.getProperties().get("val").orElseThrow().getClass());
      assertEquals(expected, item.getProperties().get("val").orElseThrow());
    }
  }
}
