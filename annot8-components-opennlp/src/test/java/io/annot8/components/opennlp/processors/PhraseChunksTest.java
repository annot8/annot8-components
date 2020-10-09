/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.references.AnnotationReference;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class PhraseChunksTest {
  @Test
  public void test() {
    // Note, this test doesn't test for the correct result,
    // but just checks that a result is produced without an exception being through.
    // The testOpenNLPExample() does check for the correct result

    Item item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class)
            .withData("Last week, Joe Bloggs was in London. Joe was seen talking to Jane Doe.")
            .save();

    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .withBounds(new SpanBounds(0, 36))
        .save();
    createWordToken(content, 0, 4, "JJ");
    createWordToken(content, 5, 9, "NN");
    createWordToken(content, 9, 10, ",");
    createWordToken(content, 11, 14, "NNP");
    createWordToken(content, 15, 21, "NNP");
    createWordToken(content, 22, 25, "VBD");
    createWordToken(content, 26, 28, "IN");
    createWordToken(content, 29, 35, "NNP");
    createWordToken(content, 35, 36, ".");

    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .withBounds(new SpanBounds(37, 70))
        .save();
    createWordToken(content, 37, 40, "NNP");
    createWordToken(content, 41, 44, "VBD");
    createWordToken(content, 45, 49, "VBN");
    createWordToken(content, 50, 57, "VBG");
    createWordToken(content, 58, 60, "TO");
    createWordToken(content, 61, 65, "NNP");
    createWordToken(content, 66, 69, "NNP");
    createWordToken(content, 69, 70, ".");

    PhraseChunks desc = new PhraseChunks();
    Processor p = desc.createComponent(null, new PhraseChunks.Settings());
    p.process(item);

    assertTrue(item.getGroups().getAll().count() > 0);

    p.close();
  }

  @Test
  public void testOpenNLPExample() {
    // Example taken from:
    // http://opennlp.apache.org/docs/1.9.3/manual/opennlp.html#tools.parser.chunking.cmdline

    Item item = new TestItem();
    TestStringContent content =
        item.createContent(TestStringContent.class)
            .withData(
                "Rockwell said the agreement calls for it to supply 200 additional so-called shipsets for the planes.")
            .save();

    String rockwell = createWordToken(content, 0, 8, "NNP");
    String said = createWordToken(content, 9, 13, "VBD");
    String the1 = createWordToken(content, 14, 17, "DT");
    String agreement = createWordToken(content, 18, 27, "NN");
    String calls = createWordToken(content, 28, 33, "VBZ");
    String for1 = createWordToken(content, 34, 37, "IN");
    String it = createWordToken(content, 38, 40, "PRP");
    String to = createWordToken(content, 41, 43, "TO");
    String supply = createWordToken(content, 44, 50, "VB");
    String num200 = createWordToken(content, 51, 54, "CD");
    String additional = createWordToken(content, 55, 65, "JJ");
    String soCalled = createWordToken(content, 66, 75, "JJ");
    String shipsets = createWordToken(content, 76, 84, "NNS");
    String for2 = createWordToken(content, 85, 88, "IN");
    String the2 = createWordToken(content, 89, 92, "DT");
    String planes = createWordToken(content, 93, 99, "NNS");
    createWordToken(content, 99, 100, ".");

    content
        .getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .withBounds(new SpanBounds(0, 100))
        .save();

    PhraseChunks desc = new PhraseChunks();
    Processor p = desc.createComponent(null, new PhraseChunks.Settings());
    p.process(item);

    List<List<String>> expectedChunks =
        List.of(
            List.of(rockwell),
            List.of(said),
            List.of(the1, agreement),
            List.of(calls),
            List.of(for1),
            List.of(it),
            List.of(to, supply),
            List.of(num200, additional, soCalled, shipsets),
            List.of(for2),
            List.of(the2, planes));

    List<List<String>> actualChunks =
        item.getGroups()
            .getAll()
            .map(
                g ->
                    g.getReferences().values().stream()
                        .flatMap(i -> i)
                        .map(AnnotationReference::getAnnotationId)
                        .collect(Collectors.toList()))
            .collect(Collectors.toList());

    assertEquals(expectedChunks.size(), actualChunks.size());
    expectedChunks.forEach(e -> assertTrue(actualChunks.stream().anyMatch(e::containsAll)));

    p.close();
  }

  private String createWordToken(Content<?> content, int begin, int end, String pos) {
    Annotation a =
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(begin, end))
            .withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, pos)
            .save();

    return a.getId();
  }
}
