/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import com.google.common.base.Strings;
import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

public class RegexSettings implements Settings {
  private final String regex;
  private final int group;
  private final String type;

  @JsonbCreator
  public RegexSettings(
      @JsonbProperty("regex") String regex,
      @JsonbProperty("group") int group,
      @JsonbProperty("type") String type) {
    this.regex = regex;
    this.group = group;
    this.type = type;
  }

  @Description("Regular expression pattern to match")
  public String getRegex() {
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
    return regex != null && group >= 0 && !Strings.isNullOrEmpty(type);
  }
}
