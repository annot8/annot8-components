/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.annot8.components.items.processors;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class ItemPropertyFromPropertyTest {
  @Test
  public void test(){
    Item item = new TestItem();
    item.getProperties().set("source", "file:///opt/foo/bar/document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    assertTrue(settings.validate());

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertTrue(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    assertEquals("bar", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
  }

  @Test
  public void testNoProperty(){
    Item item = new TestItem();
    item.getProperties().set("filename", "file:///opt/foo/bar/document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertTrue(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    assertEquals("unknown", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
  }

  @Test
  public void testObjectProperty(){
    Item item = new TestItem();
    item.getProperties().set("source", 123);

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertTrue(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    assertEquals("unknown", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
  }

  @Test
  public void testNoMatch(){
    Item item = new TestItem();
    item.getProperties().set("source", "http:///opt/foo/bar/document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertTrue(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    assertEquals("unknown", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class).get());
  }

  @Test
  public void testOutOfBoundsGroup(){
    Item item = new TestItem();
    item.getProperties().set("source", "file:///opt/foo/bar/document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(4);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    assertThrows(ProcessingException.class, () -> p.process(item));
  }

  @Test
  public void testNullGroup(){
    Item item = new TestItem();
    item.getProperties().set("source", "file:///document.txt");

    ItemPropertyFromProperty.Settings settings = new ItemPropertyFromProperty.Settings();
    settings.setSourceKey("source");
    settings.setTargetKey(PropertyKeys.PROPERTY_KEY_SUBTYPE);
    settings.setPattern(Pattern.compile("file://(/([^/]+))*/(.*\\.[a-z0-9]+)", Pattern.CASE_INSENSITIVE));
    settings.setGroup(2);
    settings.setDefaultValue("unknown");

    ItemPropertyFromProperty itfp = new ItemPropertyFromProperty();

    Processor p = itfp.createComponent(null, settings);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));

    p.process(item);

    assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
  }
}
