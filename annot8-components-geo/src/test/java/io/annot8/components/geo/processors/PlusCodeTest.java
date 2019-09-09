/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.openlocationcode.OpenLocationCode.CodeArea;

import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class PlusCodeTest {
  @Test
  public void testPlusCode() throws Annot8Exception {
    try (Processor p = new PlusCode()) {
      Item item = new TestItem();
      Context context = new TestContext();

      p.configure(context);

      Text content =
          item.create(TestStringContent.class)
              .withName("test")
              .withData("Laguna Uspaycocha can be found at 57R9C4F6+WQ.")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("57R9C4F6+WQ", a.getBounds().getData(content).get());

      Assertions.assertEquals(2, a.getProperties().getAll().size());
      Optional<Object> optValue = a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE);

      Assertions.assertTrue(optValue.isPresent());
      Assertions.assertEquals(-13.5751875, ((CodeArea) optValue.get()).getCenterLatitude(), 0.0005);
      Assertions.assertEquals(
          -72.8880625, ((CodeArea) optValue.get()).getCenterLongitude(), 0.0005);

      Optional<Object> optType = a.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE);
      Assertions.assertTrue(optType.isPresent());
      Assertions.assertEquals("Plus Code", optType.get());
    }
  }

  @Test
  public void testInvalidPlusCode() throws Annot8Exception {
    try (Processor p = new PlusCode()) {
      Item item = new TestItem();
      Context context = new TestContext();

      p.configure(context);

      Text content =
          item.create(TestStringContent.class)
              .withName("test")
              .withData("Laguna Uspaycocha can not be found at 57R9C4F+WQ.")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(0, annotations.size());
    }
  }
}
