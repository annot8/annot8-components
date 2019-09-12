/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import io.annot8.common.components.AbstractComponent;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.settings.Settings;

/** Set a property on an item to a specified value, overwriting any existing value */
public class Property extends AbstractProcessor {

  private final PropertySettings propertySettings;

  public Property(PropertySettings propertySettings) {
    this.propertySettings = propertySettings;
  }

  @Override
  public ProcessorResponse process(Item item) {
    if (propertySettings == null)
      throw new BadConfigurationException("No configuration set - have you called configure?");

    item.getProperties().set(propertySettings.getKey(), propertySettings.getValue());

    return ProcessorResponse.ok();
  }

  /** Configuration for the Property processor */
  public static class PropertySettings extends AbstractComponent implements Settings {

    private final String key;
    private final Object value;

    public PropertySettings(String key, Object value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    @Override
    public boolean validate() {
      return key != null && value != null;
    }
  }
}
