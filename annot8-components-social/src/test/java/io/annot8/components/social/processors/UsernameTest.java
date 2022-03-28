/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.social.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class UsernameTest {

  @Test
  public void testUsername() throws Annot8Exception {
    try (Processor p = new Username.Processor()) {

      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("@my-username logged in using e-mail john@example.com")
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
}
