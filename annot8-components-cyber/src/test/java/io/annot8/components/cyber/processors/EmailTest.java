/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class EmailTest {

  @Test
  public void testEmail() throws Annot8Exception {
    Email e = new Email();
    Context c = new SimpleContext();

    try (Processor p = e.createComponent(c, NoSettings.getInstance())) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("Her e-mail address was sally@example.com")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_EMAIL, a.getType());
      assertEquals(content.getId(), a.getContentId());
      assertEquals("sally@example.com", a.getBounds().getData(content).get());

      Map<String, Object> props = a.getProperties().getAll();
      assertEquals(2, props.size());
      assertEquals("sally", props.get("username"));
      assertEquals("example.com", props.get("domain"));
    }
  }
}
