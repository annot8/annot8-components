/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.components.base.utils.TypeUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

@ComponentName("Annotations to Properties")
@ComponentDescription("Creates an Item level properties based annotations across all Content")
@SettingsClass(AnnotationsToProperties.Settings.class)
public class AnnotationsToProperties
    extends AbstractProcessorDescriptor<
        AnnotationsToProperties.Processor, AnnotationsToProperties.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesAnnotations("*", Bounds.class).build();
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      Map<String, List<Annotation>> annotations =
          item.getContents()
              .flatMap(c -> c.getAnnotations().getAll())
              .filter(
                  a ->
                      settings.annotationTypes.stream()
                          .anyMatch(type -> TypeUtils.matchesWildcard(a.getType(), type)))
              .filter(a -> a.getProperties().has(settings.getPropertyNameKey()))
              .filter(a -> a.getProperties().has(settings.getPropertyValueKey()))
              .collect(
                  Collectors.groupingBy(
                      a -> {
                        String name =
                            a.getProperties()
                                .get(settings.getPropertyNameKey())
                                .orElse("**BAD**")
                                .toString();
                        if (settings.getPropertyNameTransformation()
                            == StringTransformation.CAMEL_CASE) {
                          return toCamelCase(name);
                        } else {
                          return name;
                        }
                      }));

      annotations.forEach(
          (k, a) -> {
            if (a.size() == 1) {
              Optional<Object> v = a.get(0).getProperties().get(settings.getPropertyValueKey());
              v.ifPresent(val -> item.getProperties().set(k, val));

              return;
            }

            Map<Object, Long> valueCount =
                a.stream()
                    .map(
                        annotation -> {
                          Optional<Object> v =
                              annotation.getProperties().get(settings.getPropertyValueKey());
                          return v.orElse(null);
                        })
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            if (settings.getStrategy() == Strategy.MOST_COMMON) {
              valueCount.entrySet().stream()
                  .max(Map.Entry.comparingByValue())
                  .ifPresent(e -> item.getProperties().set(k, e.getKey()));
            } else {
              valueCount.entrySet().stream()
                  .min(Map.Entry.comparingByValue())
                  .ifPresent(e -> item.getProperties().set(k, e.getKey()));
            }
          });

      return ProcessorResponse.ok();
    }

    public static String toCamelCase(String name) {
      String lcase =
          name.replaceAll("/", " ").replaceAll("[^a-zA-Z0-9 ]", "").strip().toLowerCase();

      String[] s = lcase.split(" ");

      StringJoiner sj = new StringJoiner("");
      sj.add(s[0]);

      for (int i = 1; i < s.length; i++) {
        sj.add(s[i].substring(0, 1).toUpperCase());
        sj.add(s[i].substring(1));
      }

      return sj.toString();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Strategy strategy = Strategy.MOST_COMMON;
    private List<String> annotationTypes = List.of("*");
    private String propertyNameKey = "name";
    private String propertyValueKey = "value";
    private StringTransformation propertyNameTransformation = StringTransformation.CAMEL_CASE;

    @Override
    public boolean validate() {
      return strategy != null
          && annotationTypes != null
          && !annotationTypes.isEmpty()
          && propertyNameKey != null
          && !propertyNameKey.isEmpty()
          && propertyValueKey != null
          && !propertyValueKey.isEmpty();
    }

    @Description(
        value =
            "The strategy to use when selecting the annotation. Note that where FIRST_SPAN or LAST_SPAN are used and there are multiple Content present, the ordering is not guaranteed.")
    public Strategy getStrategy() {
      return strategy;
    }

    public void setStrategy(Strategy strategy) {
      this.strategy = strategy;
    }

    @Description(
        value =
            "The types of annotation to consider, you can use * as a wildcard for a single part, or ** as a wildcard for multiple parts")
    public List<String> getAnnotationTypes() {
      return annotationTypes;
    }

    public void setAnnotationType(List<String> annotationTypes) {
      this.annotationTypes = annotationTypes;
    }

    @Description(value = "The property on the annotation to use as the name for the new property")
    public String getPropertyNameKey() {
      return propertyNameKey;
    }

    public void setPropertyNameKey(String propertyNameKey) {
      this.propertyNameKey = propertyNameKey;
    }

    @Description("The property on the annotation to use as the value for the new property")
    public String getPropertyValueKey() {
      return propertyValueKey;
    }

    public void setPropertyValueKey(String propertyValueKey) {
      this.propertyValueKey = propertyValueKey;
    }

    @Description(
        "What transformation should be applied to property values prior to creation of the new property")
    public StringTransformation getPropertyNameTransformation() {
      return propertyNameTransformation;
    }

    public void setPropertyNameTransformation(StringTransformation propertyNameTransformation) {
      this.propertyNameTransformation = propertyNameTransformation;
    }
  }

  public enum StringTransformation {
    NONE,
    CAMEL_CASE
  }

  public enum Strategy {
    MOST_COMMON,
    LEAST_COMMON
  }
}
