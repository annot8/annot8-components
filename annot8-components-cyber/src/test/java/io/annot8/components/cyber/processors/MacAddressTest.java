/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.io.BaseEncoding;
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
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class MacAddressTest {

  @Test
  public void testMacAddressHyphen() throws Annot8Exception {
    test("01-23-45-67-89-AB");
  }

  @Test
  public void testMacAddressColon() throws Annot8Exception {
    test("01:23:45:67:89:AB");
  }

  @Test
  public void testMacAddressDot() throws Annot8Exception {
    test("0123.4567.89AB");
  }

  private void test(String macAddress) throws Annot8Exception {
    MacAddress mac = new MacAddress();

    try (Processor p = mac.createComponent(new SimpleContext(), NoSettings.getInstance())) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData("The machine's MAC address was " + macAddress)
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      assertEquals(AnnotationTypes.ANNOTATION_TYPE_MACADDRESS, a.getType());
      assertEquals(content.getId(), a.getContentId());
      assertEquals(macAddress, a.getBounds().getData(content).get());

      assertEquals(1, a.getProperties().getAll().size());
      Optional<Object> optProp = a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE);

      assertTrue(optProp.isPresent());
      assertArrayEquals(BaseEncoding.base16().decode("0123456789AB"), (byte[]) optProp.get());
    }
  }
}
