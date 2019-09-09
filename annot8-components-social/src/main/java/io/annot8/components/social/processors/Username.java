/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.social.processors;

import java.util.regex.Pattern;

import io.annot8.components.base.processors.AbstractRegex;
import io.annot8.conventions.AnnotationTypes;

public class Username extends AbstractRegex {

  public Username() {
    super(
        Pattern.compile("\\B@[-_a-z0-9]+\\b", Pattern.CASE_INSENSITIVE),
        0,
        AnnotationTypes.ANNOTATION_TYPE_USERNAME);
  }
}
