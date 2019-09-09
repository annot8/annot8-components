/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import java.util.Optional;

import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.Settings;

/** Set a property on an item to a specified value, overwriting any existing value */
public class Property extends AbstractComponent implements Processor {

  private PropertySettings propertySettings = null;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    Optional<PropertySettings> opt = context.getSettings(PropertySettings.class);
    if (!opt.isPresent()) {
      throw new BadConfigurationException("Must provide a PropertySettings object");
    }

    this.propertySettings = opt.get();
  }

  @Override
  public ProcessorResponse process(Item item) throws Annot8Exception {
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
