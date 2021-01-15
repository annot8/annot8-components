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
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.utils.SortUtils;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentName("Annotation to Property")
@ComponentDescription(
    "Creates an Item level property based on the most/least common annotation across all Content")
@SettingsClass(AnnotationToProperty.Settings.class)
public class AnnotationToProperty
    extends AbstractProcessorDescriptor<
        AnnotationToProperty.Processor, AnnotationToProperty.Settings> {

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
      Object data = null;

      if (settings.getStrategy() == Strategy.FIRST_SPAN
          || settings.getStrategy() == Strategy.LAST_SPAN) {
        Map<Content<?>, Stream<Annotation>> annotations;

        if (settings.getAnnotationType() == null || settings.getAnnotationType().equals("*")) {
          annotations =
              item.getContents()
                  .collect(
                      Collectors.toMap(
                          c -> c, c -> c.getAnnotations().getByBounds(SpanBounds.class)));
        } else {
          annotations =
              item.getContents()
                  .collect(
                      Collectors.toMap(
                          c -> c,
                          c ->
                              c.getAnnotations()
                                  .getByBoundsAndType(
                                      SpanBounds.class, settings.getAnnotationType())));
        }

        Comparator<Annotation> comparator;
        if (settings.getStrategy() == Strategy.FIRST_SPAN) {
          comparator = SortUtils.SORT_BY_SPANBOUNDS;
        } else {
          comparator = SortUtils.SORT_BY_SPANBOUNDS.reversed();
        }

        for (Map.Entry<Content<?>, Stream<Annotation>> e : annotations.entrySet()) {
          data =
              e.getValue()
                  .sorted(comparator)
                  .map(
                      a -> {
                        if (settings.getAnnotationProperty() != null
                            && !settings.getAnnotationProperty().isEmpty()) {
                          return a.getProperties()
                              .get(settings.getAnnotationProperty())
                              .orElse(null);
                        } else {
                          return a.getBounds().getData(e.getKey()).orElse(null);
                        }
                      })
                  .filter(Objects::nonNull)
                  .findFirst()
                  .orElse(null);

          if (data != null) break;
        }
      } else if (settings.getStrategy() == Strategy.MOST_COMMON
          || settings.getStrategy() == Strategy.LEAST_COMMON) {
        List<Object> allData = new ArrayList<>();

        // Collect annotations from all content
        item.getContents()
            .forEach(
                c -> {
                  Stream<Annotation> annotations;
                  if (settings.getAnnotationType() == null
                      || settings.getAnnotationType().equals("*")) {
                    annotations = c.getAnnotations().getAll();
                  } else {
                    annotations = c.getAnnotations().getByType(settings.getAnnotationType());
                  }

                  annotations
                      .map(
                          a -> {
                            if (settings.getAnnotationProperty() != null
                                && !settings.getAnnotationProperty().isEmpty()) {
                              return a.getProperties().get(settings.getAnnotationProperty());
                            } else {
                              return a.getBounds().getData(c);
                            }
                          })
                      .forEach(v -> v.ifPresent(allData::add));
                });

        if (allData.isEmpty()) {
          log()
              .debug(
                  "No annotations of type {} found with accessible data",
                  settings.getAnnotationType());
          return ProcessorResponse.ok();
        }

        if (settings.getStrategy() == Strategy.MOST_COMMON) {
          Optional<Map.Entry<Object, Long>> mostCommon =
              allData.stream()
                  .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                  .entrySet()
                  .stream()
                  .max(Map.Entry.comparingByValue());

          if (mostCommon.isPresent()) data = mostCommon.get().getKey();
        } else {
          Optional<Map.Entry<Object, Long>> leastCommon =
              allData.stream()
                  .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                  .entrySet()
                  .stream()
                  .min(Map.Entry.comparingByValue());

          if (leastCommon.isPresent()) data = leastCommon.get().getKey();
        }
      } else {
        log().error("Unsupported strategy {}", settings.getStrategy());
      }

      if (data == null) {
        log()
            .warn(
                "Selected strategy {} did not identify any suitable data - property will not be set",
                settings.getStrategy());
        return ProcessorResponse.ok();
      }

      item.getProperties().set(settings.getPropertyName(), data);
      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Strategy strategy = Strategy.MOST_COMMON;
    private String annotationType = "*";
    private String propertyName = "mostCommonAnnotation";
    private String annotationProperty = null;

    @Override
    public boolean validate() {
      return strategy != null
          && annotationType != null
          && !annotationType.isEmpty()
          && propertyName != null
          && !propertyName.isEmpty();
    }

    @Description(
        value =
            "The strategy to use when selecting the annotation. Note that where FIRST_SPAN or LAST_SPAN are used and there are multiple Content present, the ordering is not guaranteed.",
        defaultValue = "MOST_COMMON")
    public Strategy getStrategy() {
      return strategy;
    }

    public void setStrategy(Strategy strategy) {
      this.strategy = strategy;
    }

    @Description(
        value = "The type of annotation to consider, or * to consider all annotations",
        defaultValue = "*")
    public String getAnnotationType() {
      return annotationType;
    }

    public void setAnnotationType(String annotationType) {
      this.annotationType = annotationType;
    }

    @Description(
        value = "The name of the property to create",
        defaultValue = "mostCommonAnnotation")
    public String getPropertyName() {
      return propertyName;
    }

    public void setPropertyName(String propertyName) {
      this.propertyName = propertyName;
    }

    @Description(
        value =
            "The name of the property on the annotation to compare. If blank, then the object covered by the annotation will be used.")
    public String getAnnotationProperty() {
      return annotationProperty;
    }

    public void setAnnotationProperty(String annotationProperty) {
      this.annotationProperty = annotationProperty;
    }
  }

  public enum Strategy {
    MOST_COMMON,
    LEAST_COMMON,
    FIRST_SPAN,
    LAST_SPAN
  }
}
