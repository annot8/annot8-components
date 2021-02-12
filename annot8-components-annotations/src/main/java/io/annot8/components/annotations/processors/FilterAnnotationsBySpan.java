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
import java.util.regex.Pattern;
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
    return new Processor(settings.getSpanValues(), settings.isCaseSensitive(), settings.isRegex());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withDeletesAnnotations("*", SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    private final List<Pattern> spanPatterns;

    public Processor(List<String> spanValues, boolean caseSensitive, boolean regex) {
      spanPatterns =
          spanValues.stream()
              .map(
                  s ->
                      Pattern.compile(
                          regex ? s : Pattern.quote(s),
                          caseSensitive ? 0 : Pattern.CASE_INSENSITIVE))
              .collect(Collectors.toList());
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

                              return spanPatterns.stream().anyMatch(p -> p.matcher(val).matches());
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
    private boolean regex;

    public Settings() {
      this.spanValues = Collections.emptyList();
      this.caseSensitive = true;
      this.regex = true;
    }

    public Settings(List<String> spanValues, boolean caseSensitive, boolean regex) {
      this.spanValues = spanValues;
      this.caseSensitive = caseSensitive;
      this.regex = regex;
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

    @Description(
        "If true, then span values will be treated as regular expression patterns rather than exact values")
    public boolean isRegex() {
      return regex;
    }

    public void setRegex(boolean regex) {
      this.regex = regex;
    }
  }
}
