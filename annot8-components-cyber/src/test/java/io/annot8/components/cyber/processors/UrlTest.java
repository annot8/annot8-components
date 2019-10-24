/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UrlTest {

  @Test
  public void testUrl() {
    Url url = new Url();
    try (Processor p = url.createComponent(new SimpleContext(), new Url.Settings())) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
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

  @Test
  public void testLenientUrl() {
    try (Processor p = new Url.Processor(true)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class)
              .withData(
                  "If you visit bbc.co.uk, then you may also want to visit bbc.co.uk/news "
                      + "or http://news.bbc.co.uk, or even news.bbc.co.uk/stories/view.php?id=123. "
                      + "But don't pull out james@example.com")
              .save();

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> annotations = store.getAll().collect(Collectors.toList());
      Assertions.assertEquals(4, annotations.size());

      Map<String, Annotation> annotationMap = new HashMap<>();
      annotations.forEach(a -> annotationMap.put(a.getBounds().getData(content).get(), a));

      Annotation a1 = annotationMap.get("bbc.co.uk");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_URL, a1.getType());
      Assertions.assertEquals(content.getId(), a1.getContentId());
      Assertions.assertEquals(0, a1.getProperties().getAll().size());

      Annotation a2 = annotationMap.get("bbc.co.uk/news");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_URL, a2.getType());
      Assertions.assertEquals(content.getId(), a2.getContentId());
      Assertions.assertEquals(0, a2.getProperties().getAll().size());

      Annotation a3 = annotationMap.get("http://news.bbc.co.uk");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_URL, a3.getType());
      Assertions.assertEquals(content.getId(), a3.getContentId());
      Assertions.assertEquals(0, a3.getProperties().getAll().size());

      Annotation a4 = annotationMap.get("news.bbc.co.uk/stories/view.php?id=123");
      Assertions.assertEquals(AnnotationTypes.ANNOTATION_TYPE_URL, a4.getType());
      Assertions.assertEquals(content.getId(), a4.getContentId());
      Assertions.assertEquals(0, a4.getProperties().getAll().size());
    }
  }
}
