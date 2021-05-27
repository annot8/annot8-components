/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

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

@ComponentName("Item Property as String")
@ComponentDescription("Converts an existing Item Property into a String")
@SettingsClass(ItemPropertyAsString.Settings.class)
@ComponentTags({"item", "properties", "string"})
public class ItemPropertyAsString
    extends AbstractProcessorDescriptor<
        ItemPropertyAsString.Processor, ItemPropertyAsString.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      if (item.getProperties().has(settings.getKey())) {
        Object o = item.getProperties().get(settings.getKey()).get();

        item.getProperties().set(settings.getKey(), o.toString());
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String key = "key";

    @Override
    public boolean validate() {
      return key != null;
    }

    @Description("The property key to convert")
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }
  }
}
