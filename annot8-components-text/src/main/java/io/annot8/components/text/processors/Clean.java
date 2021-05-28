/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.properties.Properties;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.properties.EmptyImmutableProperties;
import io.annot8.components.base.text.processors.AbstractTextProcessor;

@ComponentName("Clean Text")
@ComponentDescription("Clean up Text content")
@SettingsClass(Clean.Settings.class)
public class Clean extends AbstractProcessorDescriptor<Clean.Processor, Clean.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(Text.class)
            .withCreatesContent(Text.class);

    if (getSettings().isRemoveSourceContent()) builder = builder.withDeletesContent(Text.class);

    return builder.build();
  }

  public static class Processor extends AbstractTextProcessor {

    private final Settings settings;

    private static final String TRIM_LINES = "(\\h+\\n\\h+|\\h+\\n|\\n\\h+)";

    private static final String SPLIT_LINES = "-\\n";
    private static final String SINGLE_NEW_LINES = "(?<=\\S)\\h*\\n\\h*(?=\\S)";
    private static final String REPEATED_NEW_LINES = "(\\h*\\n\\h*){2,}";
    private static final String REPEATED_TABS = "([ \t]+\t[ \t]+|[ \t]+\t|\t[ \t]+)";

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    protected void process(Text content) {
      String clean = content.getData().replaceAll("\\r", "");

      if (settings.isTrim()) clean = clean.strip();

      if (settings.isTrimLines()) clean = clean.replaceAll(TRIM_LINES, "\n");

      if (settings.isReplaceSmartCharacters()) {
        clean =
            clean
                .replaceAll("[\u2013\u2014\u2015]", "-")
                .replaceAll("\u2017", "_")
                .replaceAll("[\u2018\u2019\u201b\u2032]", "'")
                .replaceAll("\u201a", ",")
                .replaceAll("[\u201c\u201d\u201e\u2033]", "\"")
                .replaceAll("\u2026", "...");
      }

      if (settings.isRemoveSingleNewLines()) {
        clean = clean.replaceAll(SPLIT_LINES, "-");
        clean = clean.replaceAll(SINGLE_NEW_LINES, " ");
        clean = clean.replaceAll(REPEATED_NEW_LINES, "\n\n");
      }

      if (settings.isRemoveRepeatedWhitespace()) {
        clean = clean.replaceAll(REPEATED_TABS, "\t");
        clean = clean.replaceAll(" {2,}", " ");
      }

      if (!clean.equalsIgnoreCase(content.getData())) {
        Item item = content.getItem();

        Properties props;
        if (settings.isCopyProperties()) {
          props = content.getProperties();
        } else {
          props = EmptyImmutableProperties.getInstance();
        }

        item.createContent(Text.class)
            .withDescription("Cleaned content from " + content.getId())
            .withData(clean)
            .withProperties(props)
            .save();

        // Remove source content, but only if we've made changes
        if (settings.isRemoveSourceContent()) item.removeContent(content);
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private boolean removeSourceContent = false;
    private boolean trim = true;
    private boolean trimLines = true;
    private boolean removeSingleNewLines = true;
    private boolean replaceSmartCharacters = true;
    private boolean removeRepeatedWhitespace = true;
    private boolean copyProperties = true;

    @Override
    public boolean validate() {
      return true;
    }

    @Description(
        value = "Should the source Content be removed after successful processing?",
        defaultValue = "true")
    public boolean isRemoveSourceContent() {
      return removeSourceContent;
    }

    public void setRemoveSourceContent(boolean removeSourceContent) {
      this.removeSourceContent = removeSourceContent;
    }

    @Description(
        value = "Should the text be trimmed to remove unnecessary whitespace at the start and end?",
        defaultValue = "true")
    public boolean isTrim() {
      return trim;
    }

    public void setTrim(boolean trim) {
      this.trim = trim;
    }

    @Description(
        value =
            "Should the lines be trimmed to remove unnecessary whitespace at the start and end of each line?",
        defaultValue = "true")
    public boolean isTrimLines() {
      return trimLines;
    }

    public void setTrimLines(boolean trimLines) {
      this.trimLines = trimLines;
    }

    @Description(
        value =
            "Should single new lines within text be removed? This will also reduce repeated new lines to 2 new lines.",
        defaultValue = "true")
    public boolean isRemoveSingleNewLines() {
      return removeSingleNewLines;
    }

    public void setRemoveSingleNewLines(boolean removeSingleNewLines) {
      this.removeSingleNewLines = removeSingleNewLines;
    }

    @Description(
        value =
            "Should smart characters (e.g. curly quotes) be replaced with their simpler representations?",
        defaultValue = "true")
    public boolean isReplaceSmartCharacters() {
      return replaceSmartCharacters;
    }

    public void setReplaceSmartCharacters(boolean replaceSmartCharacters) {
      this.replaceSmartCharacters = replaceSmartCharacters;
    }

    @Description(
        value =
            "Should we remove repeated white space characters, and replace with a single space or tab?",
        defaultValue = "true")
    public boolean isRemoveRepeatedWhitespace() {
      return removeRepeatedWhitespace;
    }

    public void setRemoveRepeatedWhitespace(boolean removeRepeatedWhitespace) {
      this.removeRepeatedWhitespace = removeRepeatedWhitespace;
    }

    @Description(
        value = "Should properties be copied from the source Content to the cleaned Content?",
        defaultValue = "true")
    public boolean isCopyProperties() {
      return copyProperties;
    }

    public void setCopyProperties(boolean copyProperties) {
      this.copyProperties = copyProperties;
    }
  }
}
