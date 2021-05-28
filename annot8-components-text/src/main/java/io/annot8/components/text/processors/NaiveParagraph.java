/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
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
@SettingsClass(NaiveParagraph.Settings.class)
public class NaiveParagraph
    extends AbstractProcessorDescriptor<NaiveParagraph.Processor, NaiveParagraph.Settings> {

  @Override
  protected Processor createComponent(Context context, NaiveParagraph.Settings settings) {
    return new Processor(settings.getMinimumLineBreaks());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PARAGRAPH, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private final Pattern BREAK_REGEX;

    public Processor(int minimumLineBreaks) {
      BREAK_REGEX = Pattern.compile("(\\r?\\n){" + minimumLineBreaks + ",}");
    }

    @Override
    protected void process(Text content) {
      Matcher m = BREAK_REGEX.matcher(content.getData());

      int prevIndex = 0;
      while (m.find()) {
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_PARAGRAPH)
            .withBounds(new SpanBounds(prevIndex, m.start()))
            .save();

        prevIndex = m.end();
      }

      content
          .getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_PARAGRAPH)
          .withBounds(new SpanBounds(prevIndex, content.getData().length()))
          .save();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private int minimumLineBreaks = 1;

    @Override
    public boolean validate() {
      return minimumLineBreaks > 0;
    }

    @Description("The minimum number of line breaks required between paragraphs")
    public int getMinimumLineBreaks() {
      return minimumLineBreaks;
    }

    public void setMinimumLineBreaks(int minimumLineBreaks) {
      this.minimumLineBreaks = minimumLineBreaks;
    }
  }
}
