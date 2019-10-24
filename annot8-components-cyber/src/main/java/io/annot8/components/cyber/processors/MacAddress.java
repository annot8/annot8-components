/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import com.google.common.io.BaseEncoding;
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

/** Extract MAC Addresses (EUI-48) in common formats from text */
@ComponentName("MAC Address")
@ComponentDescription("Extract MAC Addresses (EUI-48) in common formats")
public class MacAddress extends AbstractProcessorDescriptor<MacAddress.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_MACADDRESS, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {

    public Processor() {
      super(
          Pattern.compile(
              "(([0-9A-F]{2}[-:]){5}[0-9A-F]{2})|(([0-9A-F]{4}\\.){2}[0-9A-F]{4})",
              Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_MACADDRESS);
    }

    @Override
    protected void addProperties(Builder builder, Matcher m) {
      String norm = m.group(0).toUpperCase().replaceAll("[^0-9A-F]", "");
      builder.withProperty(PropertyKeys.PROPERTY_KEY_VALUE, BaseEncoding.base16().decode(norm));
    }
  }
}
