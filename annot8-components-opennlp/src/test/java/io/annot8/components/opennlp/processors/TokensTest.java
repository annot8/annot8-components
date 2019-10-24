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
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokensTest {
  @Test
  public void testBuiltInWithSentences(){
    Item item = new TestItem();
    TestStringContent content = item.createContent(TestStringContent.class)
        .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Dr. Jane Doe.")
        .save();

    content.getAnnotations().create().withBounds(new SpanBounds(0, 36)).withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE).save();
    content.getAnnotations().create().withBounds(new SpanBounds(37, 74)).withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE).save();

    Tokens tokens = new Tokens();
    Processor p = tokens.createComponent(null, new Tokens.Settings());

    p.process(item);

    List<String> annotations = new ArrayList<>();
    content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).forEach(a -> annotations.add(content.getText(a).get()));

    assertEquals(18, annotations.size());

    p.close();
  }

  @Test
  public void testBuiltInWithoutSentences(){
    Item item = new TestItem();
    TestStringContent content = item.createContent(TestStringContent.class)
        .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Dr. Jane Doe.")
        .save();

    Tokens tokens = new Tokens();
    Processor p = tokens.createComponent(null, new Tokens.Settings());

    p.process(item);

    List<String> annotations = new ArrayList<>();
    content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).forEach(a -> annotations.add(content.getText(a).get()));

    assertEquals(18, annotations.size());

    p.close();
  }
}
