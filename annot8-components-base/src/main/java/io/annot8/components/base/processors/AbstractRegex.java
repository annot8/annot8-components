/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.utils.java.StreamUtils;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.capabilities.AnnotationCapability;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.ProcessingException;
import io.annot8.core.settings.Settings;
import io.annot8.core.stores.AnnotationStore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Base class for regex annotators */
public abstract class AbstractRegex<S extends Settings> extends AbstractTextProcessor<S> {

  protected Pattern pattern = null; // TODO: Should we provide a default Pattern to avoid NPEs?
  protected int group = 0;
  protected String type = "";

  public AbstractRegex() {
    // Do nothing
  }

  public AbstractRegex(Pattern pattern, int group, String type) {
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

        Annotation.Builder builder = annotationStore.create();
        addProperties(builder, m);

        builder.withType(type).withBounds(new SpanBounds(m.start(group), m.end(group))).save();
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

  @Override
  public Stream<AnnotationCapability> createsAnnotations() {
    return StreamUtils.append(super.createsAnnotations(),
        new AnnotationCapability(type, SpanBounds.class));
  }
}
