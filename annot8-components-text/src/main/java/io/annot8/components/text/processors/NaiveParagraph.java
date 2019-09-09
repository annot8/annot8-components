/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.core.data.Item;

/**
 * Naively extract paragraphs by looking for multiple new line characters between lines using a
 * RegEx pattern.
 */
public class NaiveParagraph extends AbstractTextProcessor {

  private static final Pattern PARAGRAPH_REGEX =
      Pattern.compile("[^\\r\\n]+((\\r|\\n|\\r\\n)[^\\r\\n]+)*");

  @Override
  protected void process(Item item, Text content) {
    Matcher m = PARAGRAPH_REGEX.matcher(content.getData());
    while (m.find()) {
      content
          .getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_PARAGRAPH)
          .withBounds(new SpanBounds(m.start(), m.end()))
          .save();
    }
  }
}
