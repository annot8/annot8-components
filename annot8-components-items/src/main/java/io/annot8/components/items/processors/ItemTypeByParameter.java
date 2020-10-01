/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

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
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

@ComponentName("Item Type by Parameter")
@ComponentDescription("Explicitly sets the Item type")
@SettingsClass(ItemTypeByParameter.Settings.class)
public class ItemTypeByParameter
    extends AbstractProcessorDescriptor<
        ItemTypeByParameter.Processor, ItemTypeByParameter.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesContent(Text.class).build();
  }

  @Override
  public Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getType());
  }

  public static class Processor extends AbstractProcessor {

    private final String type;

    public Processor(String type) {
      this.type = type;
    }

    @Override
    public ProcessorResponse process(Item item) {
      if (type == null || type.isBlank()) return ProcessorResponse.ok();

      item.getProperties().set(PropertyKeys.PROPERTY_KEY_SUBTYPE, type);
      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private final String type;

    @JsonbCreator
    public Settings(@JsonbProperty("type") String type) {
      this.type = type;
    }

    @Description("Value to set type to")
    public String getType() {
      return type;
    }

    @Override
    public boolean validate() {
      return true;
    }
  }
}
