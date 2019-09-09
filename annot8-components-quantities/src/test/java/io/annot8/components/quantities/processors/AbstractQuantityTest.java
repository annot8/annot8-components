/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;

import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public abstract class AbstractQuantityTest {

  private final Class<? extends Processor> clazz;
  private final String type;
  private final String unit;

  public AbstractQuantityTest(Class<? extends Processor> clazz, String type, String unit) {
    this.clazz = clazz;
    this.type = type;
    this.unit = unit;
  }

  protected void test(String text, String expectedMatch, Double expectedValue) throws Exception {
    try (Processor p = clazz.getConstructor().newInstance()) {
      Item item = new TestItem();
      Context context = new TestContext();

      p.configure(context);

      Text content = item.create(TestStringContent.class).withName("test").withData(text).save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(type, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());

      Assertions.assertEquals(expectedMatch, a.getBounds().getData(content).get());

      Assertions.assertEquals(2, a.getProperties().getAll().size());
      Assertions.assertEquals(unit, a.getProperties().get(PropertyKeys.PROPERTY_KEY_UNIT).get());
      Assertions.assertEquals(
          expectedValue,
          (Double) a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get(),
          0.000001);
    }
  }
}
