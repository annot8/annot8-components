/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

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
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class IPv6Test {

  private void doTest(String content, String expectedMatch) throws Annot8Exception {
    try (Processor p = new IPv6()) {
      Item item = new TestItem();
      Context context = new TestContext();

      p.configure(context);

      Text c = item.create(TestStringContent.class).withName("test").withData(content).save();

      p.process(item);

      AnnotationStore store = c.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(1, annotations.size());

      Annotation a = annotations.get(0);
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_IPADDRESS, a.getType());
      Assertions.assertEquals(c.getId(), a.getContentId());
      Assertions.assertEquals(expectedMatch, a.getBounds().getData(c).get());
      Assertions.assertEquals(1, a.getProperties().getAll().size());
      Assertions.assertEquals(6, a.getProperties().get(PropertyKeys.PROPERTY_KEY_VERSION).get());
    }
  }

  @Test
  public void testFull() throws Exception {
    doTest(
        "Here's a full IPv6 address fe80:0000:0000:0000:0204:61ff:fe9d:f156 and some text after it",
        "fe80:0000:0000:0000:0204:61ff:fe9d:f156");
  }

  @Test
  public void testDropLeadingZeroes() throws Exception {
    doTest(
        "Here's an IPv6 address with leading zeroes dropped: fe80:0:0:0:204:61ff:fe9d:f156.",
        "fe80:0:0:0:204:61ff:fe9d:f156");
  }

  @Test
  public void testCollapseLeadingZeroes() throws Exception {
    doTest(
        "Here's an IPv6 address with collapsed leading zeroes: (fe80::204:61ff:fe9d:f156)",
        "fe80::204:61ff:fe9d:f156");
  }

  @Test
  public void testLocalhost() throws Exception {
    doTest("Here's the localhost IPv6 address: ::1", "::1");
  }
}
