/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

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
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class TokenizerTest {

  @Test
  public void test() throws Annot8Exception {
    Tokenizer tokenizer = new Tokenizer();
    tokenizer.configure(new TestContext());

    Item item = new TestItem();
    Text content =
        item.create(TestStringContent.class)
            .withName("test")
            .withData("Hello Mr. Bond. I've been expecting you.")
            .save();

    tokenizer.process(item, content);

    AnnotationStore store = content.getAnnotations();

    List<Annotation> sentences =
        store.getByType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE).collect(Collectors.toList());
    Assertions.assertEquals(2, sentences.size());

    sentences.sort(Comparator.comparingInt(a -> a.getBounds(SpanBounds.class).get().getBegin()));

    Annotation s1 = sentences.get(0);
    Assertions.assertEquals("Hello Mr. Bond.", s1.getBounds().getData(content).get());
    Assertions.assertEquals(0, s1.getProperties().getAll().size());

    Annotation s2 = sentences.get(1);
    Assertions.assertEquals("I've been expecting you.", s2.getBounds().getData(content).get());
    Assertions.assertEquals(0, s2.getProperties().getAll().size());

    List<Annotation> words =
        store.getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).collect(Collectors.toList());
    Assertions.assertEquals(10, words.size());

    words.sort(Comparator.comparingInt(a -> a.getBounds(SpanBounds.class).get().getBegin()));

    Annotation w1 = words.get(0);
    Assertions.assertEquals("Hello", w1.getBounds().getData(content).get());
    Assertions.assertEquals(0, w1.getProperties().getAll().size());

    Annotation w2 = words.get(1);
    Assertions.assertEquals("Mr.", w2.getBounds().getData(content).get());
    Assertions.assertEquals(0, w2.getProperties().getAll().size());

    Annotation w3 = words.get(2);
    Assertions.assertEquals("Bond", w3.getBounds().getData(content).get());
    Assertions.assertEquals(0, w3.getProperties().getAll().size());

    Annotation w4 = words.get(3);
    Assertions.assertEquals(".", w4.getBounds().getData(content).get());
    Assertions.assertEquals(0, w4.getProperties().getAll().size());
  }
}
