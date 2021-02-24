/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ComponentName("Key Value Pairs")
@ComponentDescription("Identify key value pairs within text")
@SettingsClass(KeyValuePairs.Settings.class)
public class KeyValuePairs
    extends AbstractProcessorDescriptor<KeyValuePairs.Processor, KeyValuePairs.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(getSettings().getAnnotationType(), SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private final Settings settings;
    private final Pattern keyValuePattern;

    public Processor(Settings settings) {
      this.settings = settings;

      keyValuePattern =
          Pattern.compile(
              "^\\h*(?<key>.*?)\\h*" + settings.getKeyValueSeparator() + "\\h*(?<value>.*?)\\h*$",
              Pattern.MULTILINE);
    }

    @Override
    protected void process(Text content) {
      Matcher m = keyValuePattern.matcher(content.getData());

      while (m.find()) {
        List<String> values;
        if (settings.getValueSeparator() != null && !settings.getValueSeparator().isEmpty()) {
          values =
              Arrays.stream(m.group("value").split(settings.getValueSeparator()))
                  .map(String::strip)
                  .collect(Collectors.toList());
        } else {
          values = List.of(m.group("value").strip());
        }

        Optional<Map<String, Object>> mergedProperties = Optional.empty();
        String type = null;
        if (values.size() == 1) {
          // Merge existing properties with existing annotation if we have a single value
          SpanBounds sbMatch = new SpanBounds(m.start("value"), m.end("value"));

          // Take the longest contained annotation
          Optional<Annotation> aMerge =
              content
                  .getAnnotations()
                  .getByBounds(SpanBounds.class)
                  .filter(
                      a -> {
                        SpanBounds sb = a.getBounds(SpanBounds.class).orElse(null);
                        if (sb == null) return false;

                        return sbMatch.isWithin(sb);
                      })
                  .sorted(
                      Comparator.comparingInt(
                              a -> ((Annotation) a).getBounds(SpanBounds.class).get().getLength())
                          .reversed())
                  .findFirst();

          if (aMerge.isPresent()) {
            mergedProperties = Optional.of(aMerge.get().getProperties().getAll());
            type = aMerge.get().getType();
          }
        }

        content
            .getAnnotations()
            .create()
            .withBounds(new SpanBounds(m.start("key"), m.end("value")))
            .withType(settings.getAnnotationType())
            .withPropertyIfPresent("entity", mergedProperties)
            .withPropertyIfPresent(PropertyKeys.PROPERTY_KEY_TYPE, Optional.ofNullable(type))
            .withProperty(PropertyKeys.PROPERTY_KEY_KEY, m.group("key").strip())
            .withProperty(
                PropertyKeys.PROPERTY_KEY_VALUE, values.size() > 1 ? values : values.get(0))
            .save();
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String annotationType = AnnotationTypes.ANNOTATION_TYPE_METADATA;
    private String keyValueSeparator = ":";
    private String valueSeparator = ",";

    @Override
    public boolean validate() {
      return keyValueSeparator != null && annotationType != null;
    }

    @Description("The annotation type")
    public String getAnnotationType() {
      return annotationType;
    }

    public void setAnnotationType(String annotationType) {
      this.annotationType = annotationType;
    }

    @Description(
        "The character(s) that separate the key from the value - can be a regular expression")
    public String getKeyValueSeparator() {
      return keyValueSeparator;
    }

    public void setKeyValueSeparator(String keyValueSeparator) {
      this.keyValueSeparator = keyValueSeparator;
    }

    @Description(
        "The character(s) that split values into multiple values - can be a regular expression, or null to disable")
    public String getValueSeparator() {
      return valueSeparator;
    }

    public void setValueSeparator(String valueSeparator) {
      this.valueSeparator = valueSeparator;
    }
  }
}
