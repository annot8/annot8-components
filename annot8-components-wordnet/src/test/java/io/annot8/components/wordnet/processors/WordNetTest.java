/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.wordnet.processors;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import net.sf.extjwnl.data.POS;

import org.junit.jupiter.api.Test;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class WordNetTest {
  @Test
  public void test() {
    Processor p = new WordNet.Processor();
    Item item = new TestItem();

    Text content = item.createContent(TestStringContent.class).withData("Is this working?").save();

    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(0, 2))
        .withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "VBZ")
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(3, 7))
        .withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "DT")
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(8, 15))
        .withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, "NN")
        .save();
    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
        .withBounds(new SpanBounds(15, 16))
        .withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, ".")
        .save();

    p.process(item);

    List<Annotation> annotations =
        content
            .getAnnotations()
            .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .sorted(SortUtils.SORT_BY_SPANBOUNDS)
            .collect(Collectors.toList());

    Annotation is = annotations.get(0);
    assertTrue(is.getProperties().has(PropertyKeys.PROPERTY_KEY_LEMMA));
    assertEquals("be", is.getProperties().get(PropertyKeys.PROPERTY_KEY_LEMMA, String.class).get());

    Annotation qm = annotations.get(3);
    assertFalse(qm.getProperties().has(PropertyKeys.PROPERTY_KEY_LEMMA));
  }

  @Test
  public void testToPos() {
    assertEquals(POS.VERB, WordNet.Processor.toPos("verb"));
    assertEquals(POS.VERB, WordNet.Processor.toPos("vbz"));
    assertEquals(POS.NOUN, WordNet.Processor.toPos("nns"));
    assertEquals(POS.ADVERB, WordNet.Processor.toPos("r"));
    assertEquals(POS.ADVERB, WordNet.Processor.toPos("adv"));
    assertEquals(POS.ADJECTIVE, WordNet.Processor.toPos("j"));
    assertEquals(POS.ADJECTIVE, WordNet.Processor.toPos("adj"));

    assertEquals(null, WordNet.Processor.toPos("somethingelse"));
  }
}
