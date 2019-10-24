/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.comms.processors;

import java.util.regex.Pattern;

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

@ComponentName("Telephone Number")
@ComponentDescription("Extract valid Telephone numbers from text")
public class Telephone extends AbstractProcessorDescriptor<Telephone.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {
    public Processor() {
      super(
          Pattern.compile(
              "\\b(tel|tele|telephone|phone|selector|comm)( (number|num|no))?[:. ]+([-+\\(\\) 0-9]+[0-9])\\b",
              Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER);
    }
  }
}
