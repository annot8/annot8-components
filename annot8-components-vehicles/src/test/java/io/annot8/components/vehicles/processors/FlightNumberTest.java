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
package io.annot8.components.vehicles.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlightNumberTest {

    @Test
    public void testFlightNumber() {
        try (Processor p = new FlightNumber.Processor()) {
            Item item = new TestItem();

            Text content =
                    item.createContent(TestStringContent.class)
                            .withData(
                                    "James caught flight BA22 to Baltimore. BA23 was delayed "
                                    + "(and doesn't have the word flight infront of it), and ZZ00 isn't a real flight! " +
                                            "Flight number BA1 goes to New York via Shannon.")
                            .save();

            p.process(item);

            AnnotationStore store = content.getAnnotations();

            List<Annotation> annotations = store.getAll().collect(Collectors.toList());
            Assertions.assertEquals(2, annotations.size());

            Map<String, Annotation> annotationMap = new HashMap<>();
            annotations.forEach(a -> annotationMap.put(a.getBounds().getData(content).get(), a));

            Annotation a1 = annotationMap.get("flight BA22");
            Assertions.assertEquals(AnnotationTypes.ENTITY_PREFIX+"flight", a1.getType());
            Assertions.assertEquals(content.getId(), a1.getContentId());
            Assertions.assertEquals(0, a1.getProperties().getAll().size());

            Annotation a2 = annotationMap.get("Flight number BA1");
            Assertions.assertEquals(AnnotationTypes.ENTITY_PREFIX+"flight", a2.getType());
            Assertions.assertEquals(content.getId(), a2.getContentId());
            Assertions.assertEquals(0, a2.getProperties().getAll().size());
        }
    }
}

