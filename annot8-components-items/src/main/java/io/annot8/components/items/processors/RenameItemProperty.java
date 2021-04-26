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

@ComponentName("Rename Item Property")
@ComponentDescription("Renames a property on an Item")
@SettingsClass(RenameItemProperty.Settings.class)
@ComponentTags({"item", "properties"})
public class RenameItemProperty
    extends AbstractProcessorDescriptor<RenameItemProperty.Processor, RenameItemProperty.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  @Override
  public Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getExistingKey(), settings.getNewKey());
  }

  public static class Processor extends AbstractProcessor {

    private final String existingKey;
    private final String newKey;

    public Processor(String existingKey, String newKey) {
      this.existingKey = existingKey;
      this.newKey = newKey;
    }

    @Override
    public ProcessorResponse process(Item item) {
      if (existingKey == null || existingKey.isEmpty()) return ProcessorResponse.processingError();

      if (newKey != null && !newKey.isEmpty()) {
        item.getProperties().get(existingKey).ifPresent(o -> item.getProperties().set(newKey, o));
      }

      item.getProperties().remove(existingKey);

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String existingKey;
    private String newKey;

    public Settings() {
      // Empty constructor
    }

    @JsonbCreator
    public Settings(
        @JsonbProperty("existingKey") String existingKey, @JsonbProperty("newKey") String newKey) {
      this.existingKey = existingKey;
      this.newKey = newKey;
    }

    @Override
    public boolean validate() {
      return existingKey != null;
    }

    @Description("The key of the property to be renamed")
    public String getExistingKey() {
      return existingKey;
    }

    public void setExistingKey(String existingKey) {
      this.existingKey = existingKey;
    }

    @Description("The new name for the property")
    public String getNewKey() {
      return newKey;
    }

    public void setNewKey(String newKey) {
      this.newKey = newKey;
    }
  }
}
