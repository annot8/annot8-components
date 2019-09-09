/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class UrlTest {

  @Test
  public void testUrl() throws Annot8Exception {
    try (Processor p = new Url()) {
      Item item = new TestItem();
      Context context = new TestContext();

      p.configure(context);

      Text content =
          item.create(TestStringContent.class)
              .withName("test")
              .withData(
                  "UK Government's website is http://www.gov.uk/. An example FTP directory is ftp://foo.example.com/this/is/a/path.txt. Here's a secure URL https://www.example.com/index.php?test=true . Some naughty person hasn't specified a schema here... www.example.com/path/to/page.html.")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(4, annotations.size());

      List<String> urls =
          new ArrayList<>(
              Arrays.asList(
                  "http://www.gov.uk/",
                  "ftp://foo.example.com/this/is/a/path.txt",
                  "https://www.example.com/index.php?test=true",
                  "www.example.com/path/to/page.html"));

      for (Annotation a : annotations) {
        Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_URL, a.getType());
        Assertions.assertEquals(content.getId(), a.getContentId());
        Assertions.assertEquals(0, a.getProperties().getAll().size());

        Assertions.assertTrue(urls.remove(a.getBounds().getData(content).get()));
      }

      Assertions.assertEquals(0, urls.size());
    }
  }
}
