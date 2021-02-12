/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ComponentName("Filter Annotations by Span")
@ComponentDescription("Remove all annotations with given span values")
@ComponentTags({"annotations", "filter", "type", "cleaning"})
@SettingsClass(FilterAnnotationsBySpan.Settings.class)
public class FilterAnnotationsBySpan
    extends AbstractProcessorDescriptor<
        FilterAnnotationsBySpan.Processor, FilterAnnotationsBySpan.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getSpanValues(), settings.isCaseSensitive());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withDeletesAnnotations("*", SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    private final List<String> spanValues;
    private final boolean caseSensitive;

    public Processor(List<String> spanValues, boolean caseSensitive) {
      if (caseSensitive) {
        this.spanValues = spanValues;
      } else {
        this.spanValues = spanValues.stream().map(String::toUpperCase).collect(Collectors.toList());
      }

      this.caseSensitive = caseSensitive;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents(Text.class)
          .forEach(
              c -> {
                List<Annotation> toRemove =
                    c.getAnnotations()
                        .getByBounds(SpanBounds.class)
                        .filter(
                            a -> {
                              String val = c.getText(a).orElse(null);
                              if (val == null) return false;

                              if (!caseSensitive) val = val.toUpperCase();

                              return spanValues.contains(val);
                            })
                        .collect(Collectors.toList());

                c.getAnnotations().delete(toRemove);
              });

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> spanValues;
    private boolean caseSensitive;

    public Settings() {
      this.spanValues = Collections.emptyList();
      this.caseSensitive = true;
    }

    public Settings(List<String> spanValues, boolean caseSensitive) {
      this.spanValues = spanValues;
      this.caseSensitive = caseSensitive;
    }

    @Description("Span values to remove")
    public List<String> getSpanValues() {
      return spanValues;
    }

    public void setSpanValues(List<String> types) {
      this.spanValues = types;
    }

    @Override
    public boolean validate() {
      return spanValues != null && !spanValues.isEmpty();
    }

    @Description("Are values case sensitive?")
    public boolean isCaseSensitive() {
      return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
      this.caseSensitive = caseSensitive;
    }
  }
}
