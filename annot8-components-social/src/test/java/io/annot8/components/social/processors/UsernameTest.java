/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.social.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class UsernameTest {

  @Test
  public void testUsername() throws Annot8Exception {
    Processor p = new Username();

    Item item = new TestItem();
    Context context = new TestContext();

    p.configure(context);

    Text content =
        item.create(TestStringContent.class)
            .withName("test")
            .withData("@my-username logged in")
            .save();

    p.process(item);

    AnnotationStore store = content.getAnnotations();

    List<Annotation> annotations = store.getAll().collect(Collectors.toList());
    assertEquals(1, annotations.size());

    Annotation a = annotations.get(0);
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_USERNAME, a.getType());
    assertEquals(content.getId(), a.getContentId());
    assertEquals("@my-username", a.getBounds().getData(content).get());
    assertEquals(0, a.getProperties().getAll().size());
  }
}
