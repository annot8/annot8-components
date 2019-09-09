/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.annot8.components.base.processors.AbstractRegex;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.annotations.Annotation.Builder;

public class IPv4 extends AbstractRegex {

  public IPv4() {
    super(
        Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\b"),
        0,
        AnnotationTypes.ANNOTATION_TYPE_IPADDRESS);
  }

  @Override
  protected void addProperties(Builder builder, Matcher m) {
    builder.withProperty(PropertyKeys.PROPERTY_KEY_VERSION, 4);
  }
}
