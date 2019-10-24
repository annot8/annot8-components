/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IPv4Test {

  @Test
  public void testIPv4() throws Annot8Exception {
    IPv4 ip = new IPv4();

    try (Processor p = ip.createComponent(new SimpleContext(), NoSettings.getInstance())) {
      Item item = new TestItem();

      //    Item item = new SimpleItem(itemFactory, contentBuilderFactoryRegistry);
      Text content =
          item.createContent(TestStringContent.class)
              .withData("The attack came from 127.0.0.1")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_IPADDRESS, a.getType());
      Assertions.assertEquals(content.getId(), a.getContentId());
      Assertions.assertEquals("127.0.0.1", a.getBounds().getData(content).get());
      Assertions.assertEquals(1, a.getProperties().getAll().size());
      Assertions.assertEquals(4, a.getProperties().get(PropertyKeys.PROPERTY_KEY_VERSION).get());
    }
  }
}
