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
import java.util.List;
import java.util.Optional;

@ComponentName("Item Property as Boolean")
@ComponentDescription("Converts an existing Item Property into a Boolean")
@SettingsClass(ItemPropertyAsBoolean.Settings.class)
@ComponentTags({"item", "properties", "boolean"})
public class ItemPropertyAsBoolean
    extends AbstractProcessorDescriptor<
        ItemPropertyAsBoolean.Processor, ItemPropertyAsBoolean.Settings> {

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
      Optional<Object> value = item.getProperties().get(settings.getKey());
      if (value.isPresent()) {
        Object o = value.get();

        if (!Boolean.class.isAssignableFrom(o.getClass())) {
          item.getProperties()
              .set(
                  settings.getKey(),
                  settings.getTrueValues().contains(o)
                      || settings.getTrueValues().contains(o.toString().strip().toLowerCase()));
        }
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String key = "key";
    private List<Object> trueValues = List.of("true", "yes", 1);

    @Override
    public boolean validate() {
      return key != null && trueValues != null;
    }

    @Description("The property key to convert")
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    @Description(
        "The list of values to accept as true. Comparison is done both on the original property value, and on the lower case String representation of it.")
    public List<Object> getTrueValues() {
      return trueValues;
    }

    public void setTrueValues(List<Object> trueValues) {
      this.trueValues = trueValues;
    }
  }
}
