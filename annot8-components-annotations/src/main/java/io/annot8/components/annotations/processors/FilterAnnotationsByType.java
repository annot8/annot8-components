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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ComponentName("Filter Annotations by Type")
@ComponentDescription("Remove all annotations of a given type")
@ComponentTags({"annotations", "filter", "type", "cleaning"})
@SettingsClass(FilterAnnotationsByType.Settings.class)
public class FilterAnnotationsByType
    extends AbstractProcessorDescriptor<
        FilterAnnotationsByType.Processor, FilterAnnotationsByType.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getTypes());
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder().withProcessesContent(Content.class);

    for (String type : getSettings().getTypes()) {
      builder = builder.withDeletesAnnotations(type, Bounds.class);
    }

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    private final List<String> types;

    public Processor(List<String> types) {
      this.types = types;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents()
          .forEach(
              c -> {
                types.forEach(
                    type -> {
                      List<Annotation> toRemove =
                          c.getAnnotations().getByType(type).collect(Collectors.toList());
                      log()
                          .info(
                              "Removing {} annotations of type {} from Content {}",
                              toRemove.size(),
                              type,
                              c.getDescription());

                      c.getAnnotations().delete(toRemove);
                    });
              });

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> types;

    public Settings() {
      this.types = Collections.emptyList();
    }

    public Settings(List<String> types) {
      this.types = types;
    }

    @Description("Annotation types to remove")
    public List<String> getTypes() {
      return types;
    }

    public void setTypes(List<String> types) {
      this.types = types;
    }

    @Override
    public boolean validate() {
      return types != null && !types.isEmpty();
    }
  }
}
