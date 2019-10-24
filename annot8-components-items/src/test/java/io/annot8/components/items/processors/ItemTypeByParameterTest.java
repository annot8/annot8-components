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
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ItemTypeByParameterTest {

  private Item getProcessedItem(Processor p) {
    Item item = new TestItem();
    Text content = item.createContent(TestStringContent.class)
        .withData("This is a generic document text, to test a non-content annotator.")
        .save();

    p.process(item);
    return item;
  }

  @Test
  public void testParamAssignment() {
    try (Processor p = new ItemTypeByParameter.Processor("test")) {
      Item item = getProcessedItem(p);

      Assertions.assertEquals("test", item.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE).get());
    }
  }

  @Test
  public void testNullType() {
    try (Processor p = new ItemTypeByParameter.Processor(null)) {
      Item item = getProcessedItem(p);

      Assertions.assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    }
  }

  @Test
  public void testEmptyType() {
    try (Processor p = new ItemTypeByParameter.Processor("")) {
      Item item = getProcessedItem(p);

      Assertions.assertFalse(item.getProperties().has(PropertyKeys.PROPERTY_KEY_SUBTYPE));
    }
  }


}