/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import java.util.regex.Pattern;

import io.annot8.conventions.AnnotationTypes;

public class Volume extends AbstractQuantityProcessor {

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

  public Volume() {
    super(AnnotationTypes.ANNOTATION_TYPE_VOLUME, "m^3");

    add(cm3Pattern, 0.000001);
    add(m3Pattern, 1.0);

    add(mlPattern, 0.000001);
    add(lPattern, 0.001);

    add(pintPattern, PINT_TO_M3);
    add(gallonPattern, GALLON_TO_M3);
  }
}
