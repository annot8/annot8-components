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

@ComponentName("Filter Items by Property")
@ComponentDescription("Discard items that match a specified property")
@SettingsClass(FilterItemsByProperty.Settings.class)
@ComponentTags({"item", "properties", "filter"})
public class FilterItemsByProperty
    extends AbstractProcessorDescriptor<
        FilterItemsByProperty.Processor, FilterItemsByProperty.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getKey(), settings.getValue(), settings.isDiscardMatching());
  }

  public static class Processor extends AbstractProcessor {
    private final String key;
    private final Object value;
    private final boolean discardMatching;

    public Processor(String key, Object value, boolean discardMatching) {
      this.key = key;
      this.value = value;
      this.discardMatching = discardMatching;
    }

    private boolean valueMatches(Object o) {
      return o.equals(value);
    }

    private boolean nullMatches() {
      return value == null;
    }

    @Override
    public ProcessorResponse process(Item item) {
      boolean matches =
          item.getProperties().get(key).map(this::valueMatches).orElseGet(this::nullMatches);

      if (discardMatching) {
        if (matches) {
          item.discard();
        }
      } else {
        if (!matches) {
          item.discard();
        }
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String key = null;
    private Object value = null;
    private boolean discardMatching = true;

    @Override
    public boolean validate() {
      return key != null;
    }

    @Description("The property key to match against")
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    @Description("The property value to match against")
    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    @Description(
        "If true, then matching items are discarded. Otherwise, non-matching items are discarded.")
    public boolean isDiscardMatching() {
      return discardMatching;
    }

    public void setDiscardMatching(boolean discardMatching) {
      this.discardMatching = discardMatching;
    }
  }
}
