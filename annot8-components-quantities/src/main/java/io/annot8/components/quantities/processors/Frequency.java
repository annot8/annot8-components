/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import io.annot8.api.annotations.Annotation.Builder;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Frequency")
@ComponentDescription("Extract frequencies from text")
public class Frequency extends AbstractProcessorDescriptor<Frequency.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_FREQUENCY, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private static final Pattern frequencyPattern =
        Pattern.compile("\\b(\\d+(\\.\\d+)?)\\s*([mkMGT])?Hz\\b");

    @Override
    protected void process(Text content) {
      Matcher m = frequencyPattern.matcher(content.getData());
      while (m.find()) {

        Builder builder =
            content
                .getAnnotations()
                .create()
                .withType(AnnotationTypes.ANNOTATION_TYPE_FREQUENCY)
                .withBounds(new SpanBounds(m.start(), m.end()));

        try {
          Double value = Double.parseDouble(m.group(1));
          double multiplier = 1.0;
          if (m.group(3) != null) {
            switch (m.group(3)) {
              case "m":
                multiplier = 1E-3;
                break;
              case "k":
                multiplier = 1E3;
                break;
              case "M":
                multiplier = 1E6;
                break;
              case "G":
                multiplier = 1E9;
                break;
              case "T":
                multiplier = 1E12;
                break;
            }
          }

          builder =
              builder
                  .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, value * multiplier)
                  .withProperty(PropertyKeys.PROPERTY_KEY_UNIT, "Hz");
        } catch (Exception e) {
          log().warn("Unable to parse and normalise value", e);
          e.printStackTrace();
        }

        builder.save();
      }
    }
  }
}
