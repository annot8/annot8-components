/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

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

/** Set a property on an item to a specified value, overwriting any existing value */
@ComponentName("Set Property")
@ComponentDescription("Set the value of a property on an item")
@SettingsClass(Property.Settings.class)
public class Property extends AbstractProcessorDescriptor<Property.Processor, Property.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getKey(), settings.getValue());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
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
      item.getProperties().set(key, value);

      return ProcessorResponse.ok();
    }
  }

  /** Configuration for the Property processor */
  public static class Settings implements io.annot8.api.settings.Settings {

    private final String key;
    private final Object value;

    @JsonbCreator
    public Settings(@JsonbProperty("key") String key, @JsonbProperty("value") Object value) {
      this.key = key;
      this.value = value;
    }

    @Description("Property key")
    public String getKey() {
      return key;
    }

    @Description("Property value")
    public Object getValue() {
      return value;
    }

    @Override
    public boolean validate() {
      return key != null && value != null;
    }
  }
}
