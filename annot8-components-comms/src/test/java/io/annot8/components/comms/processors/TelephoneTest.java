/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.comms.processors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class TelephoneTest {

  @Test
  public void testTelephone() {
    try (Processor p = new Telephone.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "These are valid phone numbers: tel:0113 496 0000, Tele 0116-496-0999, phone number.+442079460000; "
                      + "whereas the following are not: number +4411980958787 (no indicator of type), tell.01980952222 (wrong designator), 01980999999 (no prefix)")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(3, annotations.size());

      Map<String, Annotation> annotationMap = new HashMap<>();
      annotations.forEach(a -> annotationMap.put(a.getBounds().getData(content).get(), a));

      Annotation a1 = annotationMap.get("tel:0113 496 0000");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a1.getType());
      Assertions.assertEquals(content.getId(), a1.getContentId());
      Assertions.assertEquals(0, a1.getProperties().getAll().size());

      Annotation a2 = annotationMap.get("Tele 0116-496-0999");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a2.getType());
      Assertions.assertEquals(content.getId(), a2.getContentId());
      Assertions.assertEquals(0, a2.getProperties().getAll().size());

      Annotation a3 = annotationMap.get("phone number.+442079460000");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a3.getType());
      Assertions.assertEquals(content.getId(), a3.getContentId());
      Assertions.assertEquals(0, a3.getProperties().getAll().size());
    }
  }
}
