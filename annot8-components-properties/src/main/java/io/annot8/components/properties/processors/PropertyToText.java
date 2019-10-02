/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.exceptions.UnsupportedContentException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;

/**
 * Convert properties on an item to separate Text content so they can be processed. The toString()
 * function is used to convert properties into a String.
 */
@ComponentName("Property to Text")
@ComponentDescription("Convert Property value to Text content")
@SettingsClass(PropertyToText.Settings.class)
public class PropertyToText
    extends AbstractProcessorDescriptor<PropertyToText.Processor, PropertyToText.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getWhitelist(), settings.getBlacklist());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withCreatesContent(Text.class).build();
  }

  public static class Processor extends AbstractProcessor {

    private Set<String> whitelist;
    private Set<String> blacklist;

    public Processor(Set<String> whitelist, Set<String> blacklist) {
      if (whitelist == null) {
        this.whitelist = Collections.emptySet();
      } else {
        this.whitelist = whitelist;
      }

      if (blacklist == null) {
        this.blacklist = Collections.emptySet();
      } else {
        this.blacklist = blacklist;
      }
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getProperties().getAll().entrySet().stream()
          .filter(e -> !blacklist.contains(e.getKey())) // Key must not be on blacklist
          .filter(
              e ->
                  whitelist.isEmpty()
                      || whitelist.contains(e.getKey())) // Key must be on whitelist, if it is set
          .forEach(
              e -> {
                try {
                  item.createContent(Text.class)
                      .withDescription("Text from property from " + e.getKey())
                      .withData(e.getValue().toString())
                      .save();
                } catch (UnsupportedContentException | IncompleteException ex) {
                  log().error("Unable to create Text content", ex);
                }
              });

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Set<String> whitelist = new HashSet<>();
    private Set<String> blacklist = new HashSet<>();

    @Description(
        "Keys which will be accepted, or an empty set to accept all keys not on the blacklist")
    public Set<String> getWhitelist() {
      return whitelist;
    }

    public void setWhitelist(Set<String> whitelist) {
      this.whitelist = whitelist;
    }

    @Description("Keys which will be rejected")
    public Set<String> getBlacklist() {
      return blacklist;
    }

    public void setBlacklist(Set<String> blacklist) {
      this.blacklist = blacklist;
    }

    @Override
    public boolean validate() {
      return true;
    }
  }
}
