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
package io.annot8.components.opennlp.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NERTest {
  @Test
  public void test(){
    Item item = new TestItem();
    TestStringContent content = item.createContent(TestStringContent.class)
        .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Jane Doe.")
        .save();

    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE).withBounds(new SpanBounds(0, 36)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(0, 4)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(5, 9)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(9, 10)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(11, 14)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(15, 21)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(22, 25)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(26, 28)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(29, 35)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(35, 36)).save();

    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE).withBounds(new SpanBounds(37, 70)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(37, 40)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(41, 44)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(45, 49)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(50, 57)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(58, 60)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(61, 65)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(66, 69)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(69, 70)).save();

    Processor p = new NER.Processor(NERTest.class.getResourceAsStream("en-ner-person.bin"), AnnotationTypes.ANNOTATION_TYPE_PERSON);
    p.process(item);

    Map<String, Annotation> annotations = new HashMap<>();
    content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON).forEach(a -> annotations.put(content.getText(a).get(), a));

    assertEquals(3, annotations.size());
    assertTrue(annotations.containsKey("Joe Bloggs"));
    assertTrue(annotations.containsKey("Joe"));
    assertTrue(annotations.containsKey("Jane Doe"));

    annotations.forEach((s, a) -> {
      assertTrue(a.getProperties().get(PropertyKeys.PROPERTY_KEY_PROBABILITY).isPresent());
    });

    p.close();
  }
}
