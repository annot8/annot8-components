/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;

import java.util.regex.Pattern;

@ComponentName("Volume")
@ComponentDescription("Extract volumes from text")
public class Volume extends AbstractProcessorDescriptor<Volume.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_VOLUME, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractQuantityProcessor {

    private static final double PINT_TO_M3 = 0.000568;
    private static final double GALLON_TO_M3 = 0.00454609;

    private final Pattern m3Pattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(m\\^3|cubic metre|cubic meter)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern cm3Pattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(cm\\^3|cubic centimetre|cubic centimeter)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern lPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(l|litre|liter)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern mlPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(ml|millilitre|milliliter)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern pintPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(pt|pint)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern gallonPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(gal|gallon)(s)?\\b",
            Pattern.CASE_INSENSITIVE);

    public Processor() {
      super(AnnotationTypes.ANNOTATION_TYPE_VOLUME, "m^3");

      add(cm3Pattern, 0.000001);
      add(m3Pattern, 1.0);

      add(mlPattern, 0.000001);
      add(lPattern, 0.001);

      add(pintPattern, PINT_TO_M3);
      add(gallonPattern, GALLON_TO_M3);
    }
  }
}