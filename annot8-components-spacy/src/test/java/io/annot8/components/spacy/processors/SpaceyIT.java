/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

public class SpaceyIT {
  @Test
  public void test() {
    Item item = new TestItem();
    Text t =
        item.createContent(Text.class)
            .withData(
                "Kintzler was born in Las Vegas, and started playing baseball with a traveling youth team. "
                    + "After going undrafted out of high school, he spent one year apiece at Pasadena City College and Dixie State College, leading the latter to a national championship in 2004. "
                    + "The San Diego Padres selected Kintzler in the 40th round of the 2004 MLB Draft, and he spent two years in the team's minor league system before suffering a season-ending shoulder injury.")
            .save();

    Spacy.Settings s = new Spacy.Settings();
    s.setBaseUri("http://localhost:8000");
    try (Spacy.Processor p = new Spacy.Processor(s)) {

      ProcessorResponse r = p.process(item);
      assertEquals(ProcessorResponse.ok(), r);

      System.out.println("== Sentences ==");

      t.getAnnotations()
          .getByType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .forEach(
              a -> {
                System.out.println(a.getBounds().getData(t).orElseThrow());
              });

      System.out.println();
      System.out.println("== Tokens ==");

      t.getAnnotations()
          .getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
          .sorted(SortUtils.SORT_BY_SPANBOUNDS)
          .forEach(
              a -> {
                System.out.println(
                    a.getBounds().getData(t).orElseThrow()
                        + " ("
                        + a.getProperties()
                            .get(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH)
                            .orElseThrow()
                        + ")");
              });

      System.out.println();
      System.out.println("== Entities ==");

      t.getAnnotations()
          .getAll()
          .filter(a -> a.getType().startsWith(AnnotationTypes.ENTITY_PREFIX))
          .sorted(SortUtils.SORT_BY_SPANBOUNDS)
          .forEach(
              a -> {
                System.out.println(
                    a.getBounds().getData(t).orElseThrow() + " (" + a.getType() + ")");
              });
    }
  }
}
