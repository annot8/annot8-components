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

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NationalityTest {

    @Test
    public void testNationality() throws Annot8Exception {
        try (Processor p = new Nationality.Processor()) {

            Item item = new TestItem();

            Text content =
                    item.createContent(TestStringContent.class)
                            .withData(
                                    "James is a BRITISH national. Last month, he met an Irish bloke in the pub. He is friends with Bob, who is an Spanish.")
                            .save();

            p.process(item);

            AnnotationStore store = content.getAnnotations();

            List<Annotation> annotations = store.getAll().collect(Collectors.toList());
            Assertions.assertEquals(3, annotations.size());

            Map<String, Annotation> annotationMap = new HashMap<>();
            annotations.forEach(a -> annotationMap.put(a.getBounds().getData(content).get(), a));

            Annotation a1 = annotationMap.get("Irish");
            Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_NATIONALITY, a1.getType());
            Assertions.assertEquals(content.getId(), a1.getContentId());
            Assertions.assertEquals(2, a1.getProperties().getAll().size());
            Assertions.assertEquals("IRL", a1.getProperties().get("countryCode").get());
            Assertions.assertEquals("irish", a1.getProperties().get(PropertyKeys.PROPERTY_KEY_NATIONALITY).get());


            Annotation a2 = annotationMap.get("BRITISH");
            Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_NATIONALITY, a2.getType());
            Assertions.assertEquals(content.getId(), a2.getContentId());
            Assertions.assertEquals(2, a2.getProperties().getAll().size());
            Assertions.assertEquals("GBR", a2.getProperties().get("countryCode").get());
            Assertions.assertEquals("british", a2.getProperties().get(PropertyKeys.PROPERTY_KEY_NATIONALITY).get());

            Annotation a3 = annotationMap.get("Spanish");
            Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_NATIONALITY, a3.getType());
            Assertions.assertEquals(content.getId(), a3.getContentId());
            Assertions.assertEquals(2, a3.getProperties().getAll().size());
            Assertions.assertEquals("ESP", a3.getProperties().get("countryCode").get());
            Assertions.assertEquals("spanish", a3.getProperties().get(PropertyKeys.PROPERTY_KEY_NATIONALITY).get());
        }
    }
}
