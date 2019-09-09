/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.openlocationcode.OpenLocationCode;
import com.google.openlocationcode.OpenLocationCode.CodeArea;

import io.annot8.components.base.processors.AbstractRegex;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.annotations.Annotation.Builder;

/** Extracts full global Plus Codes (also known as Open Location Codes) from text */
public class PlusCode extends AbstractRegex {

  public PlusCode() {
    super(
        Pattern.compile(
            "\\b([23456789C][23456789CFGHJMPQRV][23456789CFGHJMPQRVWX]{6}\\+[23456789CFGHJMPQRVWX]{2,3})\\b",
            Pattern.CASE_INSENSITIVE),
        0,
        AnnotationTypes.ANNOTATION_TYPE_COORDINATE);
  }

  @Override
  protected boolean acceptMatch(Matcher m) {
    return OpenLocationCode.isValidCode(m.group()) && OpenLocationCode.isFullCode(m.group());
  }

  @Override
  protected void addProperties(Builder builder, Matcher m) {
    CodeArea ca = OpenLocationCode.decode(m.group());

    builder
        .withProperty(PropertyKeys.PROPERTY_KEY_COORDINATETYPE, "Plus Code")
        .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, ca);
  }
}
