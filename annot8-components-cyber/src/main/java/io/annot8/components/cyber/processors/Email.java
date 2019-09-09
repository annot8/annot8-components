/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import java.util.regex.Pattern;

import io.annot8.components.base.processors.AbstractRegex;
import io.annot8.conventions.AnnotationTypes;

public class Email extends AbstractRegex {

  public Email() {
    super(
        Pattern.compile("[A-Z0-9._%+-]+@([A-Z0-9.-]+[.][A-Z]{2,6})", Pattern.CASE_INSENSITIVE),
        0,
        AnnotationTypes.ANNOTATION_TYPE_EMAIL);
  }
}
