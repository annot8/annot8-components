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
package io.annot8.components.gazetteers.processors;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TermsTest {
  @Test
  public void test(){
    Terms.Settings settings = new Terms.Settings();

    settings.setType(AnnotationTypes.ANNOTATION_TYPE_PERSON);
    settings.setTerms(Arrays.asList("James", "Tom", "Tommy"));

    Terms t = new Terms();
    Processor p = t.createComponent(null, settings);

    Item item = new TestItem();

    Text content =
        item.createContent(TestStringContent.class)
            .withData("James went to visit Tom (also known as TOMMY), in London. Tommy is aged 32.")
            .save();

    p.process(item);

    assertEquals(1, item.getGroups().getAll().count());
    assertEquals(4, content.getAnnotations().getAll().count());

    List<String> annotations = content.getAnnotations().getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .sorted(SortUtils.SORT_BY_SPANBOUNDS)
        .map(a -> content.getText(a).get()).collect(Collectors.toList());

    assertEquals("James", annotations.get(0));
    assertEquals("Tom", annotations.get(1));
    assertEquals("TOMMY", annotations.get(2));
    assertEquals("Tommy", annotations.get(3));

    List<String> group = content.getItem().getGroups().getAll().findFirst().get()
        .getAnnotationsForContent(content).map(a -> content.getText(a).get())
        .collect(Collectors.toList());

    assertTrue(group.contains("TOMMY"));
    assertTrue(group.contains("Tommy"));
  }

  @Test
  public void testCaseSensitive(){
    Terms.Settings settings = new Terms.Settings();

    settings.setType(AnnotationTypes.ANNOTATION_TYPE_PERSON);
    settings.setTerms(Arrays.asList("James", "Tom", "Tommy"));
    settings.setCaseSensitive(true);

    Terms t = new Terms();
    Processor p = t.createComponent(null, settings);

    Item item = new TestItem();

    Text content =
        item.createContent(TestStringContent.class)
            .withData("James went to visit Tom (also known as TOMMY), in London.")
            .save();

    p.process(item);

    assertEquals(0, item.getGroups().getAll().count());
    assertEquals(2, content.getAnnotations().getAll().count());

    List<String> annotations = content.getAnnotations().getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .sorted(SortUtils.SORT_BY_SPANBOUNDS)
        .map(a -> content.getText(a).get()).collect(Collectors.toList());

    assertEquals("James", annotations.get(0));
    assertEquals("Tom", annotations.get(1));
  }
}
