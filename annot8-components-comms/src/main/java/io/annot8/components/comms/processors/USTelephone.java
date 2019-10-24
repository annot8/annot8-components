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

@ComponentName("US Telephone Number")
@ComponentDescription("Extract valid US-style Telephone numbers (e.g. 1-800-CALLME) from text")
public class USTelephone extends AbstractProcessorDescriptor<USTelephone.Processor, NoSettings> {

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
              "((\\(?\\+?1\\)?[-. ])?\\(?([2-9]|two|three|four|five|six|seven|eight|nine)([0-9]|zero|one|two|three|four|five|six|seven|eight|nine){2}\\)?[-. ]([2-9]|two|three|four|five|six|seven|eight|nine)([0-9]|zero|one|two|three|four|five|six|seven|eight|nine){2}[-. ]([0-9]|zero|one|two|three|four|five|six|seven|eight|nine){4}|1-800-[A-Z]{7})\\b",
              Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER);
    }
  }
}
