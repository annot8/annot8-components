/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Naively extract paragraphs by looking for multiple new line characters between lines using a
 * RegEx pattern.
 */
@ComponentName("Naive Paragraph")
@ComponentDescription(
    "Naively extract paragraphs by looking for multiple new line characters between lines")
public class NaiveParagraph
    extends AbstractProcessorDescriptor<NaiveParagraph.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PARAGRAPH, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private static final Pattern PARAGRAPH_REGEX =
        Pattern.compile("[^\\r\\n]+((\\r|\\n|\\r\\n)[^\\r\\n]+)*");

    @Override
    protected void process(Text content) {
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
}
