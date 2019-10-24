/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public abstract class AbstractQuantityTest {

  private final Class<? extends AbstractProcessorDescriptor> clazz;
  private final String type;
  private final String unit;

  public AbstractQuantityTest(Class<? extends AbstractProcessorDescriptor> clazz, String type, String unit) {
    this.clazz = clazz;
    this.type = type;
    this.unit = unit;
  }

  protected void test(String text, String expectedMatch, Double expectedValue) throws Exception {
    AbstractProcessorDescriptor pd = clazz.getConstructor().newInstance();

    try (Processor p = (Processor) pd.create(new SimpleContext())) {
      Item item = new TestItem();

      Text content = item.createContent(TestStringContent.class).withData(text).save();

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
      Assertions.assertEquals(expectedValue, (Double) a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get(),
          0.000001);
    }
  }

  protected void testCount(String text, Integer count) throws Exception {
    AbstractProcessorDescriptor pd = clazz.getConstructor().newInstance();

    try (Processor p = (Processor) pd.create(new SimpleContext())) {
      Item item = new TestItem();

      Text content = item.createContent(TestStringContent.class).withData(text).save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(count, annotations.size());
    }
  }
}
