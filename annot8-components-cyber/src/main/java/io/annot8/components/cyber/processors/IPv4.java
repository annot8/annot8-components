/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.api.annotations.Annotation.Builder;
import io.annot8.api.capabilities.Capabilities;
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

public class IPv4 extends AbstractProcessorDescriptor<IPv4.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_IPADDRESS, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {

    public Processor() {
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
}
