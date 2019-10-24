/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Sort Code")
@ComponentDescription("Extract UK sort codes from text")
public class SortCode extends AbstractProcessorDescriptor<SortCode.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_FINANCIALACCOUNT, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private static final Pattern SORT_CODE_PATTERN =
        Pattern.compile("\\b([0-9]{2})-([0-9]{2})-([0-9]{2})\\b");

    @Override
    protected void process(Text content) {
      Matcher m = SORT_CODE_PATTERN.matcher(content.getData());
      AnnotationStore annotationStore = content.getAnnotations();

      while (m.find()) {

        annotationStore
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_FINANCIALACCOUNT)
            .withBounds(new SpanBounds(m.start(), m.end()))
            .withProperty(PropertyKeys.PROPERTY_KEY_BRANCHCODE, m.group().replaceAll("-", ""))
            .save();
      }
    }
  }
}
