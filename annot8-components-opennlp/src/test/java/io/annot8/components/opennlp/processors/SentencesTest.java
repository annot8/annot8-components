/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SentencesTest {
  @Test
  public void testBuiltIn() {
    Item item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class)
            .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Dr. Jane Doe.")
            .save();

    Sentences sentence = new Sentences();
    Processor p = sentence.createComponent(null, new Sentences.Settings());

    p.process(item);

    List<String> annotations = new ArrayList<>();
    content
        .getAnnotations()
        .getByType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .forEach(a -> annotations.add(content.getText(a).get()));

    assertEquals(2, annotations.size());
    assertTrue(annotations.contains("Last week, Joe Bloggs was in London."));
    assertTrue(annotations.contains("Joe was seen talking to Dr. Jane Doe."));

    p.close();
  }
}
