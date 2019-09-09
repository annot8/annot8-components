/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class NaiveParagraphTest {

  @Test
  public void test() throws Annot8Exception {
    NaiveParagraph processor = new NaiveParagraph();

    Item item = new TestItem();
    Text content =
        item.create(TestStringContent.class)
            .withName("test")
            .withData(
                "Hello world!\n\nWhat is the square root of 64?\r\nWhy, it's 8 of course!  \nAh, right you are!\n\nHooray!")
            .save();

    processor.process(item, content);

    AnnotationStore store = content.getAnnotations();

    List<Annotation> paragraphs =
        store.getByType(AnnotationTypes.ANNOTATION_TYPE_PARAGRAPH).collect(Collectors.toList());
    Assertions.assertEquals(3, paragraphs.size());

    paragraphs.sort(Comparator.comparingInt(a -> a.getBounds(SpanBounds.class).get().getBegin()));

    Annotation p1 = paragraphs.get(0);
    Assertions.assertEquals("Hello world!", p1.getBounds().getData(content).get());
    Assertions.assertEquals(0, p1.getProperties().getAll().size());

    Annotation p2 = paragraphs.get(1);
    Assertions.assertEquals(
        "What is the square root of 64?\r\nWhy, it's 8 of course!  \nAh, right you are!",
        p2.getBounds().getData(content).get());
    Assertions.assertEquals(0, p2.getProperties().getAll().size());

    Annotation p3 = paragraphs.get(2);
    Assertions.assertEquals("Hooray!", p3.getBounds().getData(content).get());
    Assertions.assertEquals(0, p3.getProperties().getAll().size());
  }
}
