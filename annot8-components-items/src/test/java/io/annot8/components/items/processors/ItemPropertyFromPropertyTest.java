/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;

public class ItemPropertyFromPropertyTest {
  @Test
  public void test() {
    Item item = new TestItem();
    item.getProperties().set("source", "file:///opt/foo/bar/document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(
        Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    assertTrue(settings.validate());

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertTrue(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    assertEquals(
        "bar", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
  }

  @Test
  public void testNoProperty() {
    Item item = new TestItem();
    item.getProperties().set("filename", "file:///opt/foo/bar/document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(
        Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertTrue(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    assertEquals(
        "unknown", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
  }

  @Test
  public void testObjectProperty() {
    Item item = new TestItem();
    item.getProperties().set("source", 123);

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(
        Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertTrue(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    assertEquals(
        "unknown", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
  }

  @Test
  public void testNoMatch() {
    Item item = new TestItem();
    item.getProperties().set("source", "http:///opt/foo/bar/document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(
        Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertTrue(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    assertEquals(
        "unknown", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
  }

  @Test
  public void testOutOfBoundsGroup() {
    Item item = new TestItem();
    item.getProperties().set("source", "file:///opt/foo/bar/document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(
        Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(4);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    assertThrows(ProcessingException.class, () -> p.process(item));
  }

  @Test
  public void testNullGroup() {
    Item item = new TestItem();
    item.getProperties().set("source", "file:///document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(
        Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
  }
}
