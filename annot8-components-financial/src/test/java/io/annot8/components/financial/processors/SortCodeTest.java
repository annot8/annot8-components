/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class SortCodeTest {

  @Test
  public void testSortCode() throws Annot8Exception {
    try (Processor p = new SortCode.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class).withData("The sort code was 77-49-09").save();

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
