/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;

@ComponentName("Item Property from Property")
@ComponentDescription(
    "Applies a regular expression to an existing item property and sets another item property based on the result")
@SettingsClass(ItemPropertyFromProperty.Settings.class)
public class ItemPropertyFromProperty
    extends AbstractProcessorDescriptor<
        ItemPropertyFromProperty.Processor, ItemPropertyFromProperty.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(
        settings.getSourceKey(),
        settings.getTargetKey(),
        settings.getPattern(),
        settings.getGroup(),
        settings.getDefaultValue());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  public static class Processor extends AbstractProcessor {

    private final String sourceKey;
    private final String targetKey;
    private final Pattern pattern;
    private final int group;
    private final String defaultValue;

    public Processor(
        String sourceKey, String targetKey, Pattern pattern, int group, String defaultValue) {
      if (sourceKey == null || sourceKey.isBlank())
        throw new BadConfigurationException("Source key can't be null or blank");

      if (targetKey == null || targetKey.isBlank())
        throw new BadConfigurationException("Target key can't be null or blank");

      if (pattern == null) throw new BadConfigurationException("Pattern must be set");

      if (group < 0) throw new BadConfigurationException("Group number must be 0 or greater");

      if (defaultValue == null)
        log()
            .info(
                "No default value set - target property will not be set if pattern does not match");

      this.sourceKey = sourceKey;
      this.targetKey = targetKey;
      this.pattern = pattern;
      this.group = group;
      this.defaultValue = defaultValue;
    }

    @Override
    public ProcessorResponse process(Item item) {
      if (item.getProperties().has(sourceKey, String.class)) {
        String s = item.getProperties().get(sourceKey, String.class).get();
        Matcher m = pattern.matcher(s);
        if (m.matches()) {
          try {
            String t = m.group(group);
            if (t != null) item.getProperties().set(targetKey, t);
          } catch (IndexOutOfBoundsException e) {
            throw new ProcessingException("Group number out of bounds for provided pattern", e);
          }
        } else {
          if (defaultValue != null) item.getProperties().set(targetKey, defaultValue);
        }
      } else {
        if (defaultValue != null) item.getProperties().set(targetKey, defaultValue);
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {

    private String sourceKey;
    private String targetKey;
    private Pattern pattern = Pattern.compile(".*", Pattern.CASE_INSENSITIVE);
    private int group = 0;
    private String defaultValue = null;

    @Description("The property key to read")
    public String getSourceKey() {
      return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
      this.sourceKey = sourceKey;
    }

    @Description("The property key to write")
    public String getTargetKey() {
      return targetKey;
    }

    public void setTargetKey(String targetKey) {
      this.targetKey = targetKey;
    }

    @Description("The RegEx to apply to the property pattern")
    public Pattern getPattern() {
      return pattern;
    }

    public void setPattern(Pattern pattern) {
      this.pattern = pattern;
    }

    @Description("Which RegEx group to use for the type")
    public int getGroup() {
      return group;
    }

    public void setGroup(int group) {
      this.group = group;
    }

    @Description(
        "The default type to use if the RegEx doesn't match or the property isn't a String (null for ignore)")
    public String getDefaultValue() {
      return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
    }

    @Override
    public boolean validate() {
      return sourceKey != null && targetKey != null && pattern != null && group >= 0;
    }
  }
}
