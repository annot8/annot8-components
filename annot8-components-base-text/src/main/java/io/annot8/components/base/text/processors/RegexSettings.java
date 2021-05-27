/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.text.processors;

import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import java.util.regex.Pattern;

public class RegexSettings implements Settings {
  private final Pattern regex;
  private final int group;
  private final String type;

  @JsonbCreator
  public RegexSettings(
      @JsonbProperty("regex") Pattern regex,
      @JsonbProperty("group") int group,
      @JsonbProperty("type") String type) {
    this.regex = regex;
    this.group = group;
    this.type = type;
  }

  @Description("Regular expression pattern to match")
  public Pattern getRegex() {
    return regex;
  }

  @Description("The group that should be annotated (0 for the whole expression)")
  public int getGroup() {
    return group;
  }

  @Description("The type of annotation to create")
  public String getType() {
    return type;
  }

  @Override
  public boolean validate() {
    return regex != null && group >= 0 && type != null && !type.isBlank();
  }
}
