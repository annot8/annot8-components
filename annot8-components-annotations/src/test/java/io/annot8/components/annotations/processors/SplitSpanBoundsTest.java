/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class SplitSpanBoundsTest {
  @Test
  public void test() {
    Item i = new TestItem();
    Text t =
        i.createContent(Text.class)
            .withData("He spoke to Alice, Bob and Charlie that night")
            .save();

    t.getAnnotations()
        .create()
        .withBounds(new SpanBounds(12, 34))
        .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .withProperty("test", 123)
        .save();

    t.getAnnotations()
        .create()
        .withBounds(new SpanBounds(12, 34))
        .withType("entity/test")
        .withProperty("test", 456)
        .save();

    SplitSpanBounds.Settings s = new SplitSpanBounds.Settings();
    s.setSplit("( and|,) ?");
    s.setTypes(Set.of(AnnotationTypes.ANNOTATION_TYPE_PERSON));

    SplitSpanBounds.Processor p = new SplitSpanBounds.Processor(s);

    ProcessorResponse pr = p.process(i);
    assertEquals(ProcessorResponse.ok(), pr);

    assertEquals(3L, t.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON).count());
    assertEquals(1, t.getAnnotations().getByType("entity/test").count());

    List<String> spans =
        t.getAnnotations()
            .getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .map(
                a ->
                    a.getBounds(SpanBounds.class)
                        .orElse(new SpanBounds(0, 0))
                        .getData(t, String.class)
                        .orElse(""))
            .collect(Collectors.toList());

    assertTrue(spans.contains("Alice"));
    assertTrue(spans.contains("Bob"));
    assertTrue(spans.contains("Charlie"));

    t.getAnnotations()
        .getByType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
        .forEach(a -> assertEquals(123, (int) a.getProperties().getOrDefault("test", 0)));
  }
}
