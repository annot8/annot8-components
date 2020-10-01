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

@ComponentName("Filter Annotations by Property")
@ComponentDescription("Remove all annotations with a given property value")
@ComponentTags({"annotations", "filter", "property", "cleaning"})
@SettingsClass(FilterAnnotationsByProperty.Settings.class)
public class FilterAnnotationsByProperty
    extends AbstractProcessorDescriptor<
        FilterAnnotationsByProperty.Processor, FilterAnnotationsByProperty.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getKey(), settings.getValue());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withDeletesAnnotations("*", Bounds.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    private final String key;
    private final Object value;

    public Processor(String key, Object value) {
      this.key = key;
      this.value = value;
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
                              Optional<Object> opt = a.getProperties().get(key);
                              return opt.isPresent() && opt.get().equals(value);
                            })
                        .collect(Collectors.toList());

                log()
                    .info(
                        "Removing {} annotations from Content {}",
                        toRemove.size(),
                        c.getDescription());
                c.getAnnotations().delete(toRemove);
              });

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String key;
    private Object value;

    public Settings(){
      this.key = null;
      this.value = null;
    }

    public Settings(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    @Description("Property key to check")
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    @Description("Property value to check")
    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    @Override
    public boolean validate() {
      return key != null;
    }
  }
}
