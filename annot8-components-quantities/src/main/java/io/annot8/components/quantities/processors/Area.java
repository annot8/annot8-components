/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import java.util.regex.Pattern;

import io.annot8.conventions.AnnotationTypes;

public class Area extends AbstractQuantityProcessor {

  private static final double MM2_TO_M2 = 0.000001;
  private static final double CM2_TO_M2 = 0.0001;
  private static final double KM2_TO_M2 = 1000000.0;

  private static final double MI2_TO_M2 = 2589988.1;
  private static final double YD2_TO_M2 = 0.83612739;
  private static final double FT2_TO_M2 = 0.092903044;
  private static final double IN2_TO_M2 = 0.00064516;

  private static final double ACRE_TO_M2 = 4046.8564;
  private static final double HECTARE_TO_M2 = 10000.0;

  private final Pattern m2Pattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(m\\^2|square metre|square meter|square m)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern mm2Pattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(mm\\^2|square millimetre|square millimeter|square mm)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern cm2Pattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(cm\\^2|square centimetre|square centimeter|square cm)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern km2Pattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(km\\^2|square kilometre|square kilometers|square km)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern mi2Pattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(mi\\^2|square miles|square mi)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern yd2Pattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(yd\\^2|square yard|square yd)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern ft2Pattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(ft\\^2|square foot|square feet|square ft)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern in2Pattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(in\\^2|square inch|square in|square inche)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern haPattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(hectare|ha)(s)?\\b",
          Pattern.CASE_INSENSITIVE);
  private final Pattern acrePattern =
      Pattern.compile(
          "\\b([0-9]+([0-9\\.,]+[0-9])?)[ ]?(thousand|million|billion|trillion)?[ ]?(acre)(s)?\\b",
          Pattern.CASE_INSENSITIVE);

  public Area() {
    super(AnnotationTypes.ANNOTATION_TYPE_AREA, "m^2");

    add(mm2Pattern, MM2_TO_M2);
    add(cm2Pattern, CM2_TO_M2);
    add(m2Pattern, 1.0);
    add(km2Pattern, KM2_TO_M2);

    add(in2Pattern, IN2_TO_M2);
    add(ft2Pattern, FT2_TO_M2);
    add(yd2Pattern, YD2_TO_M2);
    add(mi2Pattern, MI2_TO_M2);

    add(acrePattern, ACRE_TO_M2);
    add(haPattern, HECTARE_TO_M2);
  }
}
