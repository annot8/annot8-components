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

public class OSGBTest {

  @Test
  public void testOsgb() throws Annot8Exception {
    try (Processor p = new OSGB.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "Ben Nevis is located at NN 166 712. The car park is located at NN126729. " +
                      "The event took place at GR SU 02194 45374")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(3, annotations.size());

      Map<String, Annotation> annotationMap = new HashMap<>();
      annotations.forEach(a -> annotationMap.put(a.getBounds().getData(content).get(), a));

      Annotation a1 = annotationMap.get("NN 166 712");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a1.getType());
      Assertions.assertEquals(content.getId(), a1.getContentId());
      Assertions.assertEquals(4, a1.getProperties().getAll().size());
      Assertions.assertEquals("osgb", a1.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
      Assertions.assertEquals("{\"type\": \"Point\", \"coordinates\": [-5.004712,56.794800]}", a1.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
      Assertions.assertEquals(56.794800, a1.getProperties().get(PropertyKeys.PROPERTY_KEY_LATITUDE, Double.class).get(),0.00001);
      Assertions.assertEquals(-5.004712, a1.getProperties().get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Double.class).get(),0.00001);

      Annotation a2 = annotationMap.get("NN126729");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a2.getType());
      Assertions.assertEquals(content.getId(), a2.getContentId());
      Assertions.assertEquals(4, a2.getProperties().getAll().size());
      Assertions.assertEquals("osgb", a2.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
      Assertions.assertEquals("{\"type\": \"Point\", \"coordinates\": [-5.071352,56.808457]}", a2.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
      Assertions.assertEquals(56.808457, a2.getProperties().get(PropertyKeys.PROPERTY_KEY_LATITUDE, Double.class).get(),0.00001);
      Assertions.assertEquals(-5.071352, a2.getProperties().get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Double.class).get(),0.00001);

      Annotation a3 = annotationMap.get("SU 02194 45374");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a3.getType());
      Assertions.assertEquals(content.getId(), a3.getContentId());
      Assertions.assertEquals(4, a3.getProperties().getAll().size());
      Assertions.assertEquals("osgb", a3.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
      Assertions.assertEquals("{\"type\": \"Point\", \"coordinates\": [-1.969975,51.206197]}", a3.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
      Assertions.assertEquals(51.206197, a3.getProperties().get(PropertyKeys.PROPERTY_KEY_LATITUDE, Double.class).get(),0.00001);
      Assertions.assertEquals(-1.969975, a3.getProperties().get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Double.class).get(),0.00001);

    }
  }
}
