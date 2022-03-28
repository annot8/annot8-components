/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.testing.testimpl.TestItem;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

public class ItemPropertyAsStringTest {

  @Test
  public void test() {
    test("Hello", "Hello");
    test(123, "123");
    test(LocalDate.of(2021, Month.APRIL, 28), "2021-04-28");
    test(true, "true");
  }

  private void test(Object orig, String expected) {
    Item item = new TestItem();
    item.getProperties().set("val", orig);

    ItemPropertyAsString.Settings s = new ItemPropertyAsString.Settings();
    s.setKey("val");

    try (ItemPropertyAsString.Processor p = new ItemPropertyAsString.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(item));

      assertTrue(item.getProperties().has("val"));
      assertEquals(String.class, item.getProperties().get("val").orElseThrow().getClass());
      assertEquals(expected, item.getProperties().get("val").orElseThrow());
    }
  }
}
