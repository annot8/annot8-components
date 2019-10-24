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
package io.annot8.components.comms.processors;

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

public class TelephoneTest {

    @Test
    public void testTelephone() {
        try (Processor p = new Telephone.Processor()) {
            Item item = new TestItem();

            Text content =
                    item.createContent(TestStringContent.class)
                            .withData(
                                    "These are valid phone numbers: tel:0113 496 0000, Tele 0116-496-0999, phone number.+442079460000; "
                                            + "whereas the following are not: number +4411980958787 (no indicator of type), tell.01980952222 (wrong designator), 01980999999 (no prefix)")
                            .save();

            p.process(item);

            AnnotationStore store = content.getAnnotations();

            List<Annotation> annotations = store.getAll().collect(Collectors.toList());
            Assertions.assertEquals(3, annotations.size());

            Map<String, Annotation> annotationMap = new HashMap<>();
            annotations.forEach(a -> annotationMap.put(a.getBounds().getData(content).get(), a));

            Annotation a1 = annotationMap.get("tel:0113 496 0000");
            Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a1.getType());
            Assertions.assertEquals(content.getId(), a1.getContentId());
            Assertions.assertEquals(0, a1.getProperties().getAll().size());

            Annotation a2 = annotationMap.get("Tele 0116-496-0999");
            Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a2.getType());
            Assertions.assertEquals(content.getId(), a2.getContentId());
            Assertions.assertEquals(0, a2.getProperties().getAll().size());

            Annotation a3 = annotationMap.get("phone number.+442079460000");
            Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a3.getType());
            Assertions.assertEquals(content.getId(), a3.getContentId());
            Assertions.assertEquals(0, a3.getProperties().getAll().size());
        }
    }
}
