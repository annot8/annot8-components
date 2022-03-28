/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class RemoveCollectionStopwordsTest {

  @Test
  public void test() {
    RemoveCollectionStopwords.Settings s = new RemoveCollectionStopwords.Settings();
    s.setLanguage("en");
    s.setTypes(List.of("entity/location"));
    s.setStopwords(Set.of("here", "there", "everywhere"));

    RemoveCollectionStopwords rcs = new RemoveCollectionStopwords();
    Processor p = rcs.createComponent(new SimpleContext(), s);

    Item item = new TestItem();

    TestStringContent tsc =
        item.createContent(TestStringContent.class).withData("Bob has been there.").save();

    tsc.getAnnotations().create().withBounds(new SpanBounds(0, 3)).withType("entity/person").save();

    tsc.getAnnotations()
        .create()
        .withBounds(new SpanBounds(13, 18))
        .withType("entity/location")
        .save();

    p.process(item);

    assertEquals(1L, tsc.getAnnotations().getByType("entity/person").count());
    assertEquals(0L, tsc.getAnnotations().getByType("entity/location").count());
  }
}
