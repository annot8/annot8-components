/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.social.processors;

import java.util.regex.Pattern;

import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;

public class HashTag extends AbstractRegexProcessor {

  public HashTag() {
    super(
        Pattern.compile("#[a-z0-9]+", Pattern.CASE_INSENSITIVE),
        0,
        AnnotationTypes.ANNOTATION_TYPE_HASHTAG);
  }
}
