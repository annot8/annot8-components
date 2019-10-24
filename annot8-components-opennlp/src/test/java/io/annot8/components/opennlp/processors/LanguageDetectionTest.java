/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class LanguageDetectionTest {
  @Test
  public void testEnglish() {
    // Content taken from: http://www.gutenberg.org/files/60327/60327-0.txt
    test(
        "The fog had swallowed up the house, and the house had submitted."
            + " So thick was this fog that the towers of Westminster Abbey, the river, and the fat complacency of the church in the middle of the Square,"
            + " even the three Plane Trees in front of the old gate and the heavy old-fashioned porch had all vanished together, leaving in their place,"
            + " the rattle of a cab, the barking of a dog, isolated sounds that ascended, plaintively, from a lost, a submerged world.",
        "eng");
  }

  @Test
  public void testGerman() {
    // Content taken from: http://www.gutenberg.org/cache/epub/6004/pg6004.txt
    test(
        "Das Schlafzimmer eines jungen Mädchens in Bulgarien, in einer kleinen Stadt nahe dem Dragomanpaß. "
            + "Ende November 1885. Durch ein großes offenes Fenster mit kleinem Balkon schimmert sternhell die schneebedeckte Spitze eines Balkanberges wundervoll weiß und schön herein. "
            + "Das Gebirge scheint ganz nahe, obwohl es in Wirklichkeit meilenweit entfernt ist.",
        "deu");
  }

  @Test
  public void testGreek() {
    // Content taken from: http://www.gutenberg.org/files/36300/36300-0.txt
    test(
        "Τη λεπτή, την χαριτωμένη αυτή ομιλία έκαναν ανάμεσό τους — ποιος ήθελε το πιστέψη ποτέ — "
            + "ένας γυιός και μια μάννα, στον αυλόγυρο ενός μοναστηριού, μια πρωινή απολείτουργα."
            + " Το παράξενο δε είνε πως αυτό δεν ήταν εχθρικό πετροβόλημα, όπως λογικά θα υπόθετε καθένας· ήταν, "
            + "όλο το εναντίο, ένα παιχνίδι μαλακό, αθόρυβο, όλως διόλου ακίνδυνο, σαν να έπαιζαν το τόπι τα δυο υποκείμενα.",
        "ell");
  }

  private void test(String text, String expectedLanguage) {
    Item item = new TestItem();
    TestStringContent content = item.createContent(TestStringContent.class).withData(text).save();

    LanguageDetection desc = new LanguageDetection();
    Processor p = desc.createComponent(null, new LanguageDetection.Settings());
    p.process(item);

    List<Annotation> l =
        content.getAnnotations().getByBounds(ContentBounds.class).collect(Collectors.toList());

    assertEquals(1, l.size());
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_LANGUAGE, l.get(0).getType());
    assertTrue(l.get(0).getProperties().has(PropertyKeys.PROPERTY_KEY_PROBABILITY));
    assertEquals(
        expectedLanguage,
        l.get(0)
            .getProperties()
            .get(PropertyKeys.PROPERTY_KEY_LANGUAGE, String.class)
            .orElse(null));

    p.close();
  }
}
