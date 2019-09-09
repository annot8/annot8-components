/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.settings.EmptySettings;
import io.annot8.core.settings.SettingsClass;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class SortCodeTest {
  @Test
  public void testSettings() {
    SettingsClass annotation = SortCode.class.getAnnotation(SettingsClass.class);
    Assertions.assertEquals(EmptySettings.class, annotation.value());
  }

  @Test
  public void testSortCode() throws Annot8Exception {
    try (Processor p = new SortCode()) {
      Item item = new TestItem();
      Context context = new TestContext();

      p.configure(context);

      Text content =
          item.create(TestStringContent.class)
              .withName("test")
              .withData("The sort code was 77-49-09")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_FINANCIALACCOUNT, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("77-49-09", a.getBounds().getData(content).get());
      Assertions.assertEquals(1, a.getProperties().getAll().size());
      Assertions.assertEquals(
          "774909", a.getProperties().get(PropertyKeys.PROPERTY_KEY_BRANCHCODE).get());
    }
  }
}
