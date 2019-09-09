/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class DetectLanguageTest {

  @Test
  public void testDetectLanguageEnglish() throws Annot8Exception {
    // Taken from Pride and Prejudice
    doTest(
        "It is a truth universally acknowledged, that a single man in possession of a good fortune, must be in want of a wife.\n"
            + "However little known the feelings or views of such a man may be on his first entering a neighbourhood, this truth is so "
            + "well fixed in the minds of the surrounding families, that he is considered the rightful property of some one or other of their daughters.",
        "en");
  }

  @Test
  public void testDetectLanguageGerman() throws Annot8Exception {
    // Taken from Der Mord an der Jungfrau
    doTest(
        "Immerzu traurig, Amaryllis! sollten dich die jungen Herrn im Stich\n"
            + "gelassen haben, deine Blüten welk, deine Wohlgerüche ausgehaucht sein? Ließ\n"
            + "Atys, das göttliche Kind, von dir mit seinen eitlen Liebkosungen?",
        "de");
  }

  private void doTest(String sourceText, String expectedLanguage) throws Annot8Exception {
    try (Processor p = new DetectLanguage()) {
      Item item = new TestItem();
      Context context = new TestContext();

      p.configure(context);

      Text content =
          item.create(TestStringContent.class).withName("test").withData(sourceText).save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      assertEquals(ContentBounds.getInstance(), a.getBounds());
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_LANGUAGE, a.getType());

      assertEquals(1, a.getProperties().getAll().size());
      Optional<Object> o = a.getProperties().get(PropertyKeys.PROPERTY_KEY_LANGUAGE);
      assertTrue(o.isPresent());
      assertEquals(expectedLanguage, o.get());
    }
  }
}
