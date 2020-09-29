/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.api.annotations.Annotation.Builder;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("CVE")
@ComponentDescription("Extracts Common Vulnerabilities and Exposure identifiers from text")
@ComponentTags({"cyber", "cve", "text"})
public class CVE extends AbstractProcessorDescriptor<CVE.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_VULNERABILITY, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {

    public Processor() {

      super(
          Pattern.compile("\\bCVE-([0-9]{4})-([0-9]+)\\b", Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_VULNERABILITY);
    }

    @Override
    protected void addProperties(Builder builder, Matcher m) {
      builder.withProperty("year", Integer.parseInt(m.group(1)));
      builder.withProperty(PropertyKeys.PROPERTY_KEY_IDENTIFIER, m.group().toUpperCase());
    }
  }
}
