/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import java.util.HashSet;
import java.util.Set;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.components.properties.processors.PropertyToText.PropertyToTextSettings;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.exceptions.UnsupportedContentException;
import io.annot8.core.settings.Settings;
import io.annot8.core.settings.SettingsClass;

/**
 * Convert properties on an item to separate Text content so they can be processed. The toString()
 * function is used to convert properties into a String.
 */
@CreatesContent(Text.class)
@SettingsClass(PropertyToTextSettings.class)
public class PropertyToText extends AbstractComponent implements Processor {

  private PropertyToTextSettings settings;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    settings =
        context.getSettings(PropertyToTextSettings.class).orElseGet(PropertyToTextSettings::new);
  }

  @Override
  public ProcessorResponse process(Item item) {
    item.getProperties()
        .getAll()
        .entrySet()
        .stream()
        .filter(e -> !settings.getBlacklist().contains(e.getKey())) // Key must not be on blacklist
        .filter(
            e ->
                settings.getWhitelist().isEmpty()
                    || settings
                        .getWhitelist()
                        .contains(e.getKey())) // Key must be on whitelist, if it is set
        .forEach(
            e -> {
              try {
                item.create(Text.class)
                    .withName(e.getKey())
                    .withData(e.getValue().toString())
                    .save();
              } catch (UnsupportedContentException | IncompleteException ex) {
                log().error("Unable to create Text content", ex);
              }
            });

    return ProcessorResponse.ok();
  }

  public static class PropertyToTextSettings implements Settings {
    private Set<String> whitelist = new HashSet<>();
    private Set<String> blacklist = new HashSet<>();

    public Set<String> getWhitelist() {
      return whitelist;
    }

    public void setWhitelist(Set<String> whitelist) {
      this.whitelist = whitelist;
    }

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
