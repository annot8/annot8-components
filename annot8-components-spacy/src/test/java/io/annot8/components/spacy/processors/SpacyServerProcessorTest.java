/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

public class SpacyServerProcessorTest {
  @Test
  public void testToNerLabel() {
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_PERSON, SpacyServerProcessor.toNerLabel("PERSON"));
    assertEquals(
        AnnotationTypes.ENTITY_PREFIX + "foobar", SpacyServerProcessor.toNerLabel("FOOBAR"));
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_ENTITY, SpacyServerProcessor.toNerLabel(""));
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_ENTITY, SpacyServerProcessor.toNerLabel(null));
  }

  @Test
  public void testFromTextContent() {
    Item item = new TestItem();
    Text t = item.createContent(Text.class).withData("Hello World!").save();

    org.openapi.spacy.model.Text text = SpacyServerProcessor.fromTextContent(t);

    assertEquals("Hello World!", text.getText());
  }
}
