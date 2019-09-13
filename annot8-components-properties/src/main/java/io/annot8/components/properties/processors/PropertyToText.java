/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import java.util.HashSet;
import java.util.Set;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.exceptions.UnsupportedContentException;
import io.annot8.api.settings.Settings;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.data.content.Text;

/**
 * Convert properties on an item to separate Text content so they can be processed. The toString()
 * function is used to convert properties into a String.
 */
public class PropertyToText extends AbstractProcessor {

  private PropertyToTextSettings settings;

  public PropertyToText(PropertyToTextSettings settings) {
    this.settings = settings;
  }

  @Override
  public ProcessorResponse process(Item item) {
    item.getProperties().getAll().entrySet().stream()
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
