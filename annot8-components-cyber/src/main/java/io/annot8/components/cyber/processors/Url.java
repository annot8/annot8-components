/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import java.util.regex.Pattern;

import io.annot8.components.base.processors.AbstractRegex;
import io.annot8.conventions.AnnotationTypes;

public class Url extends AbstractRegex {

  public Url() {
    super(
        Pattern.compile(
            "\\b((https?|ftp)://|www.)(([-a-z0-9]+)\\.)?([-a-z0-9.]+\\.[a-z0-9]+)(:([1-9][0-9]{1,5}))?(/([-a-z0-9+&@#/%=~_|$!:,.]*\\?[-a-z0-9+&@#/%=~_|$!:,.]*)|/([-a-z0-9+&@#/%=~_|$!:,.]*[-a-z0-9+&@#/%=~_|$!:,])|/)?",
            Pattern.CASE_INSENSITIVE),
        0,
        AnnotationTypes.ANNOTATION_TYPE_URL);
  }
}
