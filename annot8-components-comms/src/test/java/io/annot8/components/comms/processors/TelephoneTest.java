/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.comms.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class TelephoneTest {

  @Test
  public void testTelephone() {
    try (Processor p =
        new Telephone.Processor(new Telephone.Settings("GB", PhoneNumberUtil.Leniency.VALID))) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "Look at all these phone numbers: tel:0113 496 0000, 0116-496-0999, phone number.+442079460000")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      assertEquals(3, annotations.size());

      Map<String, Annotation> annotationMap = new HashMap<>();
      annotations.forEach(a -> annotationMap.put(a.getBounds().getData(content).get(), a));

      Annotation a1 = annotationMap.get("0113 496 0000");
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a1.getType());
      assertEquals(content.getId(), a1.getContentId());
      assertEquals(2, a1.getProperties().getAll().size());
      assertEquals("+441134960000", a1.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
      assertEquals(
          PhoneNumberUtil.PhoneNumberType.FIXED_LINE,
          a1.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE).get());

      Annotation a2 = annotationMap.get("0116-496-0999");
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a2.getType());
      assertEquals(content.getId(), a2.getContentId());
      assertEquals(2, a2.getProperties().getAll().size());
      assertEquals("+441164960999", a2.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
      assertEquals(
          PhoneNumberUtil.PhoneNumberType.FIXED_LINE,
          a2.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE).get());

      Annotation a3 = annotationMap.get("+442079460000");
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, a3.getType());
      assertEquals(content.getId(), a3.getContentId());
      assertEquals(2, a3.getProperties().getAll().size());
      assertEquals("+442079460000", a3.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
      assertEquals(
          PhoneNumberUtil.PhoneNumberType.FIXED_LINE,
          a3.getProperties().get(PropertyKeys.PROPERTY_KEY_SUBTYPE).get());
    }
  }
}
