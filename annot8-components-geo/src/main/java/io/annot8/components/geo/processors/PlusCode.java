/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import com.google.openlocationcode.OpenLocationCode;
import com.google.openlocationcode.OpenLocationCode.CodeArea;
import io.annot8.api.annotations.Annotation.Builder;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts full global Plus Codes (also known as Open Location Codes) from text */
@ComponentName("Plus Code")
@ComponentDescription("Extracts full global Plus Codes (also known as Open Location Codes) from text")
public class PlusCode extends AbstractProcessorDescriptor<PlusCode.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {
    public Processor() {
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
}
