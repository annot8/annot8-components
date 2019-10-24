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
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SentencesTest {
  @Test
  public void testBuiltIn(){
    Item item = new TestItem();
    TestStringContent content = item.createContent(TestStringContent.class)
        .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Dr. Jane Doe.")
        .save();

    Sentences sentence = new Sentences();
    Processor p = sentence.createComponent(null, new Sentences.Settings());

    p.process(item);

    List<String> annotations = new ArrayList<>();
    content.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE).forEach(a -> annotations.add(content.getText(a).get()));

    assertEquals(2, annotations.size());
    assertTrue(annotations.contains("Last week, Joe Bloggs was in London."));
    assertTrue(annotations.contains("Joe was seen talking to Dr. Jane Doe."));

    p.close();
  }
}
