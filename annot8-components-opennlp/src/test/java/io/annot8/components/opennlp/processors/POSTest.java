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

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class POSTest {
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

    POS desc = new POS();
    Processor p = desc.createComponent(null, new POS.Settings());
    p.process(item);

    assertEquals(17, content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).count());
    assertEquals(17, content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).filter(a -> a.getProperties().has(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH)).count());

    p.close();
  }
}
