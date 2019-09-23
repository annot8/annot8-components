/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;

import java.util.regex.Pattern;

@ComponentName("URL")
@ComponentDescription("Extract URLs")
public class Url extends AbstractProcessorDescriptor<Url.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_URL, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {
    public Processor() {
      super(
          Pattern.compile(
              "\\b((https?|ftp)://|www.)(([-a-z0-9]+)\\.)?([-a-z0-9.]+\\.[a-z0-9]+)(:([1-9][0-9]{1,5}))?(/([-a-z0-9+&@#/%=~_|$!:,.]*\\?[-a-z0-9+&@#/%=~_|$!:,.]*)|/([-a-z0-9+&@#/%=~_|$!:,.]*[-a-z0-9+&@#/%=~_|$!:,])|/)?",
              Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_URL);
    }
  }
}
