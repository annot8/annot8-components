/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ComponentName("Filter Annotations by Numerical Property")
@ComponentDescription(
    "Remove all annotations where a numerical property value meets a given criteria")
@ComponentTags({"annotations", "filter", "property", "cleaning"})
@SettingsClass(FilterAnnotationsByNumericalProperty.Settings.class)
public class FilterAnnotationsByNumericalProperty
    extends AbstractProcessorDescriptor<
        FilterAnnotationsByNumericalProperty.Processor,
        FilterAnnotationsByNumericalProperty.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withDeletesAnnotations("*", Bounds.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents()
          .forEach(
              c -> {
                List<Annotation> toRemove =
                    c.getAnnotations()
                        .filter(
                            a -> {
                              Optional<Object> opt = a.getProperties().get(settings.getKey());
                              if (opt.isEmpty()) return !settings.isIgnoreNonNumeric();

                              double value;
                              try {
                                Object obj = opt.get();
                                if (obj instanceof Double) {
                                  value = (Double) obj;
                                } else {
                                  value = Double.parseDouble(obj.toString());
                                }
                              } catch (NumberFormatException nfe) {
                                return !settings.isIgnoreNonNumeric();
                              }

                              switch (settings.getOperator()) {
                                case GREATER_THAN:
                                  return value > settings.getValue();
                                case GREATER_THAN_OR_EQUAL:
                                  return value >= settings.getValue();
                                case LESS_THAN:
                                  return value < settings.getValue();
                                case LESS_THAN_OR_EQUAL:
                                  return value <= settings.getValue();
                                case EQUALS:
                                  return value == settings.getValue();
                              }

                              return false;
                            })
                        .collect(Collectors.toList());

                if (!toRemove.isEmpty()) {
                  log()
                      .info(
                          "Removing {} annotations from Content {}",
                          toRemove.size(),
                          c.getDescription());
                  c.getAnnotations().delete(toRemove);
                }
              });

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String key;
    private double value;
    private Operator operator;
    private boolean ignoreNonNumeric;

    public Settings() {
      this.key = null;
      this.value = 0.0;
      this.operator = Operator.GREATER_THAN;
      this.ignoreNonNumeric = false;
    }

    public Settings(String key, double value, Operator operator, boolean ignoreNonNumeric) {
      this.key = key;
      this.value = value;
      this.operator = operator;
      this.ignoreNonNumeric = ignoreNonNumeric;
    }

    @Description("Property key to check")
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    @Description("Number to compare the property against against")
    public double getValue() {
      return value;
    }

    public void setValue(double value) {
      this.value = value;
    }

    @Description("The operator to apply when comparing the value")
    public Operator getOperator() {
      return operator;
    }

    public void setOperator(Operator operator) {
      this.operator = operator;
    }

    @Description(
        "If true, then non-numeric values will be ignored. If false, non-numeric values will be filtered.")
    public boolean isIgnoreNonNumeric() {
      return ignoreNonNumeric;
    }

    public void setIgnoreNonNumeric(boolean ignoreNonNumeric) {
      this.ignoreNonNumeric = ignoreNonNumeric;
    }

    @Override
    public boolean validate() {
      return key != null && operator != null;
    }
  }

  public enum Operator {
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    EQUALS
  }
}
