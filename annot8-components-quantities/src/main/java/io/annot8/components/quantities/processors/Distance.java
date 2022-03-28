/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import java.util.regex.Pattern;

@ComponentName("Distance")
@ComponentDescription("Extract distances from text")
@ComponentTags({"quantity", "distance", "text"})
public class Distance extends AbstractProcessorDescriptor<Distance.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_DISTANCE, SpanBounds.class)
        .build();
  }

  // Deep type hierarchy
  @SuppressWarnings({"java:S110", "java:S5852"})
  public static class Processor extends AbstractQuantityProcessor {

    private static final double MI_TO_M = 1609.344;
    private static final double YD_TO_M = 0.9144;
    private static final double FT_TO_M = 0.3048;
    private static final double IN_TO_M = 0.0254;
    private static final double NM_TO_M = 1852.0; // Nautical miles

    private final Pattern kmPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(km|kilometre|kilometer|click)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern mPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(m|metre|meter)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern cmPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(cm|centimetre|centimeter)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern mmPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(mm|millimetre|millimeter)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern miPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(mile)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern ydPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(yard|yd)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern ftPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(foot|feet|ft)\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern inPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(inch|inches)\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern nmPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(hundred|thousand|million|billion|trillion)?[ ]?(nm|nmi|nautical mile(s)?)\\b",
            Pattern.CASE_INSENSITIVE);

    public Processor() {
      super(AnnotationTypes.ANNOTATION_TYPE_DISTANCE, "m");

      add(mmPattern, 0.001);
      add(cmPattern, 0.01);
      add(mPattern, 1.0);
      add(kmPattern, 1000.0);

      add(inPattern, IN_TO_M);
      add(ftPattern, FT_TO_M);
      add(ydPattern, YD_TO_M);
      add(miPattern, MI_TO_M);
      add(nmPattern, NM_TO_M);
    }
  }
}
