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

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhraseChunksTest {
  @Test
  public void test() {
    Item item = new TestItem();
    TestStringContent content = item.createContent(TestStringContent.class)
        .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Jane Doe.")
        .save();

    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE).withBounds(new SpanBounds(0, 36)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(0, 4)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "JJ").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(5, 9)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "NN").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(9, 10)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, ",").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(11, 14)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "NNP").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(15, 21)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "NNP").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(22, 25)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "VBD").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(26, 28)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "IN").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(29, 35)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "NNP").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(35, 36)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, ".").save();

    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE).withBounds(new SpanBounds(37, 70)).save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(37, 40)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "NNP").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(41, 44)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "VBD").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(45, 49)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "VBN").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(50, 57)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "VBG").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(58, 60)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "TO").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(61, 65)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "NNP").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(66, 69)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "NNP").save();
    content.getAnnotations().create().withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).withBounds(new SpanBounds(69, 70)).withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, ".").save();

    PhraseChunks desc = new PhraseChunks();
    Processor p = desc.createComponent(null, new PhraseChunks.Settings());
    p.process(item);

    assertTrue(item.getGroups().getAll().count() > 0);

    p.close();
  }
}
