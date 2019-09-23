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

@ComponentName("Mass")
@ComponentDescription("Extract mass from text")
public class Mass extends AbstractProcessorDescriptor<Mass.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_MASS, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractQuantityProcessor {

    private static final double LONG_TON_TO_KG = 1016.0469088;
    private static final double STONE_TO_KG = 6.35029318;
    private static final double POUNDS_TO_KG = 0.45359237;
    private static final double OUNCES_TO_KG = 0.028349523125;

    private final Pattern tonnePattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(tonne)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern kgPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(kg|kilogram|kilo)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern gPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(g|gram)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern mgPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(mg|milligram)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern tonPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(ton)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern lbPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(lb)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern stonePattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(st|stone)(s)?\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern ozPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(oz|ounce)(s)?\\b",
            Pattern.CASE_INSENSITIVE);

    public Processor() {
      super(AnnotationTypes.ANNOTATION_TYPE_MASS, "kg");

      add(tonnePattern, 1000.0);
      add(kgPattern, 1.0);
      add(gPattern, 0.001);
      add(mgPattern, 0.000001);
      add(tonPattern, LONG_TON_TO_KG);
      add(lbPattern, POUNDS_TO_KG);
      add(stonePattern, STONE_TO_KG);
      add(ozPattern, OUNCES_TO_KG);
    }
  }
}
