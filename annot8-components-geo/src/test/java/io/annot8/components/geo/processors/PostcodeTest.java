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
package io.annot8.components.geo.processors;

import io.annot8.api.components.Processor;
import io.annot8.api.properties.ImmutableProperties;
import io.annot8.api.settings.NoSettings;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PostcodeTest {

    @Test
    public void testPorton(){
        TestItem testItem = new TestItem();
        TestStringContent content = testItem.createContent(TestStringContent.class)
                .withData("Porton Down is located at SP4 0JQ.")
                .save();

        Postcode postcode = new Postcode();
        Processor postcodeProcessor = postcode.createComponent(null, NoSettings.getInstance());
        postcodeProcessor.process(testItem);

        ImmutableProperties result = content.getAnnotations().getAll().findAny().orElseThrow().getProperties();
        assertEquals("SP40JQ", result.getOrDefault("postcode", ""));
        assertEquals(-1.6988, result.getOrDefault(PropertyKeys.PROPERTY_KEY_LONGITUDE, 0.0), 0.001);
        assertEquals(51.1346, result.getOrDefault(PropertyKeys.PROPERTY_KEY_LATITUDE, 0.0), 0.001);
    }

    @Test
    public void testWrongPorton() {
        TestItem testItem = new TestItem();
        TestStringContent content = testItem.createContent(TestStringContent.class)
                .withData("Porton Down is not located at JP4 0JQ.")
                .save();

        Postcode postcode = new Postcode();
        Processor postcodeProcessor = postcode.createComponent(null, NoSettings.getInstance());
        postcodeProcessor.process(testItem);

        ImmutableProperties result = content.getAnnotations().getAll().findAny().orElseThrow().getProperties();
        assertFalse(result.has("postcode"));
        assertFalse(result.has(PropertyKeys.PROPERTY_KEY_LONGITUDE));
        assertFalse(result.has(PropertyKeys.PROPERTY_KEY_LATITUDE));
    }
}
