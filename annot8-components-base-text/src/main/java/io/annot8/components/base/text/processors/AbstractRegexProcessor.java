/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.text.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Base class for regex annotators */
public abstract class AbstractRegexProcessor extends AbstractTextProcessor {

  protected final Pattern pattern;
  protected final int group;
  protected final String type;

  protected AbstractRegexProcessor(Pattern pattern, String type) {
    this.pattern = pattern;
    this.group = 0;
    this.type = type;
  }

  protected AbstractRegexProcessor(Pattern pattern, int group, String type) {
    this.pattern = pattern;
    this.group = group;
    this.type = type;
  }

  @Override
  protected void process(Text content) {
    if (pattern == null) {
      throw new BadConfigurationException("Parameter 'pattern' must not be null");
    }

    AnnotationStore annotationStore = content.getAnnotations();

    Matcher m = pattern.matcher(content.getData());
    while (m.find()) {
      if (!acceptMatch(m)) {
        continue;
      }

      try {

        Annotation.Builder builder =
            annotationStore
                .create()
                .withType(type)
                .withBounds(new SpanBounds(m.start(group), m.end(group)));
        addProperties(builder, m);

        builder.save();
      } catch (IndexOutOfBoundsException e) {
        throw new ProcessingException("Invalid group", e);
      }
    }
  }

  protected void addProperties(Annotation.Builder builder, Matcher m) {
    // Do nothing
  }

  protected boolean acceptMatch(final Matcher m) {
    return true;
  }
}
