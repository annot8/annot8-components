/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import static org.assertj.core.api.Assertions.fail;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.api.annotations.Annotation;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.exceptions.UnsupportedContentException;

public abstract class AbstractSinkTest {

  protected Content addContent(Item item, String desc, String data) {
    Content content = null;
    try {
      content = item.createContent(Text.class).withDescription(desc).withData(data).save();
    } catch (UnsupportedContentException | IncompleteException e) {
      fail("Test should not fail creating content", e);
    }
    return content;
  }

  protected Annotation addAnnotation(Content content, String type, int begin, int end) {
    Annotation annotation = null;
    try {
      annotation =
          content
              .getAnnotations()
              .create()
              .withBounds(new SpanBounds(begin, end))
              .withType(type)
              .save();
    } catch (IncompleteException e) {
      fail("Test should not fail creating annotations", e);
    }
    return annotation;
  }
}
