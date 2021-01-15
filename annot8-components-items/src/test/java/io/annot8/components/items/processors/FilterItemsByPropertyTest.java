/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

public class FilterItemsByPropertyTest {
  @Test
  public void testDiscardMatching() {
    Item itemMatch = new TestItem();
    Item itemNoMatch = new TestItem();
    Item itemNotPresent = new TestItem();

    itemMatch.getProperties().set("foo", "bar");
    itemNoMatch.getProperties().set("foo", "baz");

    FilterItemsByProperty.Processor p = new FilterItemsByProperty.Processor("foo", "bar", true);

    assertEquals(ProcessorResponse.ok(), p.process(itemMatch));
    assertTrue(itemMatch.isDiscarded());

    assertEquals(ProcessorResponse.ok(), p.process(itemNoMatch));
    assertFalse(itemNoMatch.isDiscarded());

    assertEquals(ProcessorResponse.ok(), p.process(itemNotPresent));
    assertFalse(itemNotPresent.isDiscarded());
  }

  @Test
  public void testDiscardMatchingNull() {
    Item itemPresent = new TestItem();
    Item itemNotPresent = new TestItem();

    itemPresent.getProperties().set("foo", "bar");

    FilterItemsByProperty.Processor p = new FilterItemsByProperty.Processor("foo", null, true);

    assertEquals(ProcessorResponse.ok(), p.process(itemPresent));
    assertFalse(itemPresent.isDiscarded());

    assertEquals(ProcessorResponse.ok(), p.process(itemNotPresent));
    assertTrue(itemNotPresent.isDiscarded());
  }

  @Test
  public void testDiscardNotMatching() {
    Item itemMatch = new TestItem();
    Item itemNoMatch = new TestItem();
    Item itemNotPresent = new TestItem();

    itemMatch.getProperties().set("foo", "bar");
    itemNoMatch.getProperties().set("foo", "baz");

    FilterItemsByProperty.Processor p = new FilterItemsByProperty.Processor("foo", "bar", false);

    assertEquals(ProcessorResponse.ok(), p.process(itemMatch));
    assertFalse(itemMatch.isDiscarded());

    assertEquals(ProcessorResponse.ok(), p.process(itemNoMatch));
    assertTrue(itemNoMatch.isDiscarded());

    assertEquals(ProcessorResponse.ok(), p.process(itemNotPresent));
    assertTrue(itemNotPresent.isDiscarded());
  }

  @Test
  public void testDiscardNotMatchingNull() {
    Item itemPresent = new TestItem();
    Item itemNotPresent = new TestItem();

    itemPresent.getProperties().set("foo", "bar");

    FilterItemsByProperty.Processor p = new FilterItemsByProperty.Processor("foo", null, false);

    assertEquals(ProcessorResponse.ok(), p.process(itemPresent));
    assertTrue(itemPresent.isDiscarded());

    assertEquals(ProcessorResponse.ok(), p.process(itemNotPresent));
    assertFalse(itemNotPresent.isDiscarded());
  }

  @Test
  public void testSettings() {
    FilterItemsByProperty.Settings s = new FilterItemsByProperty.Settings();

    assertFalse(s.validate());

    s.setKey("test");
    assertEquals("test", s.getKey());

    assertTrue(s.validate());

    s.setValue(123);
    assertEquals(123, s.getValue());

    s.setDiscardMatching(true);
    assertTrue(s.isDiscardMatching());

    s.setDiscardMatching(false);
    assertFalse(s.isDiscardMatching());
  }

  @Test
  public void testCapabilities() {
    FilterItemsByProperty fibp = new FilterItemsByProperty();
    assertNotNull(fibp.capabilities());
  }

  @Test
  public void testCreate() {
    FilterItemsByProperty.Settings s = new FilterItemsByProperty.Settings();
    s.setKey("test");

    FilterItemsByProperty fibp = new FilterItemsByProperty();

    assertNotNull(fibp.createComponent(new SimpleContext(), s));
  }
}
