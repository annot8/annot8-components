/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.comms.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class USTelephoneTest {

  @Test
  public void testUSTelephone() {
    try (Processor p = new USTelephone.Processor()) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "Call on 234-235-5678, Call on (234)-235-5678, Call on 234.235.5678, Call on 234 235 5678,"
                      + "Call on +1 234-235-5678, Call on (+1)-234-235-5678, Call on +1-(234)-235-5678,"
                      + "Call on 1-800-567-4567, Call on 234-2three5-56seven8, Call on 1-800-DENTIST,"
                      + "Don't call on 014-459-2653 (First group can't start with a 0 or 1), "
                      + "Don't call on 314-159-2653 (Second group can't start with a 0 or 1)")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(10, annotations.size());

      Map<String, Annotation> annotationMap = new HashMap<>();
      annotations.forEach(a -> annotationMap.put(a.getBounds().getData(content).get(), a));

      Annotation a1 = annotationMap.get("234-235-5678");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a1.getType());
      Assertions.assertEquals(content.getId(), a1.getContentId());
      Assertions.assertEquals(0, a1.getProperties().getAll().size());

      Annotation a2 = annotationMap.get("(234)-235-5678");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a2.getType());
      Assertions.assertEquals(content.getId(), a2.getContentId());
      Assertions.assertEquals(0, a2.getProperties().getAll().size());

      Annotation a3 = annotationMap.get("234.235.5678");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a3.getType());
      Assertions.assertEquals(content.getId(), a3.getContentId());
      Assertions.assertEquals(0, a3.getProperties().getAll().size());

      Annotation a4 = annotationMap.get("234 235 5678");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a4.getType());
      Assertions.assertEquals(content.getId(), a4.getContentId());
      Assertions.assertEquals(0, a4.getProperties().getAll().size());

      Annotation a5 = annotationMap.get("+1 234-235-5678");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a5.getType());
      Assertions.assertEquals(content.getId(), a5.getContentId());
      Assertions.assertEquals(0, a5.getProperties().getAll().size());

      Annotation a6 = annotationMap.get("(+1)-234-235-5678");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a6.getType());
      Assertions.assertEquals(content.getId(), a6.getContentId());
      Assertions.assertEquals(0, a6.getProperties().getAll().size());

      Annotation a7 = annotationMap.get("+1-(234)-235-5678");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a7.getType());
      Assertions.assertEquals(content.getId(), a7.getContentId());
      Assertions.assertEquals(0, a7.getProperties().getAll().size());

      Annotation a8 = annotationMap.get("1-800-567-4567");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a8.getType());
      Assertions.assertEquals(content.getId(), a8.getContentId());
      Assertions.assertEquals(0, a8.getProperties().getAll().size());

      Annotation a9 = annotationMap.get("234-2three5-56seven8");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a9.getType());
      Assertions.assertEquals(content.getId(), a9.getContentId());
      Assertions.assertEquals(0, a9.getProperties().getAll().size());

      Annotation a10 = annotationMap.get("1-800-DENTIST");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a10.getType());
      Assertions.assertEquals(content.getId(), a10.getContentId());
      Assertions.assertEquals(0, a10.getProperties().getAll().size());
    }
  }
}
