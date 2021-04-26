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
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

@ComponentName("Item Property from Parameter")
@ComponentDescription("Explicitly sets a property on the item")
@SettingsClass(ItemPropertyFromParameter.Settings.class)
@ComponentTags({"item", "properties"})
public class ItemPropertyFromParameter
    extends AbstractProcessorDescriptor<
        ItemPropertyFromParameter.Processor, ItemPropertyFromParameter.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  @Override
  public Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getKey(), settings.getValue());
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
      if (key == null || key.isEmpty()) return ProcessorResponse.processingError();

      if (value == null) {
        item.getProperties().remove(key);
      } else {
        item.getProperties().set(key, value);
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String key;
    private Object value;

    public Settings() {
      // Empty constructor
    }

    @JsonbCreator
    public Settings(@JsonbProperty("key") String key, @JsonbProperty("value") Object value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public boolean validate() {
      return key != null;
    }

    @Description("The key of the property to set")
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    @Description("The value of the property to set, or null to remove the property")
    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }
  }
}
