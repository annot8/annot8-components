/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.groups.processors;

import io.annot8.api.annotations.Group;
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
import io.annot8.components.base.utils.TypeUtils;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ComponentName("Filter Groups by Type")
@ComponentDescription("Remove all groups of a given type")
@ComponentTags({"groups", "filter", "type", "cleaning"})
@SettingsClass(FilterGroupsByType.Settings.class)
public class FilterGroupsByType
    extends AbstractProcessorDescriptor<FilterGroupsByType.Processor, FilterGroupsByType.Settings> {

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
      types.forEach(
          type -> {
            List<Group> toRemove = getGroups(item, type);

            if (toRemove.isEmpty()) return;

            log()
                .info(
                    "Removing {} groups of type {} from Item {}",
                    toRemove.size(),
                    type,
                    item.getId());

            item.getGroups().delete(toRemove);
          });

      return ProcessorResponse.ok();
    }

    private static List<Group> getGroups(Item item, String type) {
      if (type.contains("*")) {
        // Wildcard, so we need to loop through everything
        return item.getGroups()
            .getAll()
            .filter(g -> TypeUtils.matchesWildcard(g.getType(), type))
            .collect(Collectors.toList());
      } else {
        // No wildcard, so use the exact type to quickly get group
        return item.getGroups().getByType(type).collect(Collectors.toList());
      }
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

    @Description(
        "Group types to remove - you can use * as a wildcard for a single part, or ** as a wildcard for multiple parts")
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
