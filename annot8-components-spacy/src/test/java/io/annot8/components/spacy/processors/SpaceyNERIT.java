/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class SpaceyNERIT {
  @Test
  public void test() {
    Item item = new TestItem();
    Text t =
        item.createContent(Text.class)
            .withData("Bill Gates founded Microsoft in 1975 with Paul Allen.")
            .save();

    SpacyServerSettings s = new SpacyServerSettings();
    s.setBaseUri("http://localhost:8000");
    SpacyNER.Processor p = new SpacyNER.Processor(s);

    ProcessorResponse r = p.process(item);
    assertEquals(ProcessorResponse.ok(), r);

    List<Annotation> a = t.getAnnotations().getAll().collect(Collectors.toList());
    assertEquals(4, a.size());

    List<String> as =
        a.stream()
            .map(an -> an.getType() + ": " + an.getBounds().getData(t).get())
            .collect(Collectors.toList());

    assertTrue(as.contains("entity/person: Bill Gates"));
    assertTrue(as.contains("entity/person: Paul Allen"));
    assertTrue(as.contains("entity/organisation: Microsoft"));
    assertTrue(as.contains("entity/temporal: 1975"));
  }
}
