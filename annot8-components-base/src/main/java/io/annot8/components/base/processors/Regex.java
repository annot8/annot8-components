/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import com.google.common.base.Strings;
import io.annot8.components.base.processors.Regex.RegexSettings;
import io.annot8.core.components.Resource;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.Settings;

import java.util.Map;
import java.util.regex.Pattern;

/** Base class for regex annotators */
public class Regex extends AbstractRegex<RegexSettings> {

  public Regex() {
    // Do nothing
  }

  public Regex(Pattern pattern, int group, String type) {
    this.pattern = pattern;
    this.group = group;
    this.type = type;
  }

  @Override
  public void configure(RegexSettings settings, Map<String, Resource> resources) throws BadConfigurationException, MissingResourceException {
    if (!settings.validate()) {
      throw new BadConfigurationException("Regex settings are invalid");
    }

    this.pattern = settings.getRegex();
    this.group = settings.getGroup();
    this.type = settings.getType();
  }

  public static class RegexSettings implements Settings {

    private final Pattern regex;
    private final int group;
    private final String type;

    public RegexSettings(Pattern regex, int group, String type) {
      this.regex = regex;
      this.group = group;
      this.type = type;
    }

    public Pattern getRegex() {
      return regex;
    }

    public int getGroup() {
      return group;
    }

    public String getType() {
      return type;
    }

    @Override
    public boolean validate() {
      return regex != null && group >= 0 && !Strings.isNullOrEmpty(type);
    }
  }
}
