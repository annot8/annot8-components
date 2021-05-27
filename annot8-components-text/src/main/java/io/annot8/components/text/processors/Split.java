/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.PropertyKeys;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Split Text")
@ComponentDescription("Split Text content into separate contents")
@SettingsClass(Split.Settings.class)
public class Split extends AbstractProcessorDescriptor<Split.Processor, Split.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(
        settings.getSplitOn(), settings.isRemoveSourceContent(), settings.getGroupProperties());
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

    private final Pattern splitOn;
    private final boolean removeSourceContent;
    private final Map<Integer, String> groupProperties;

    public Processor(
        Pattern splitOn, boolean removeSourceContent, Map<Integer, String> groupProperties) {
      this.splitOn = splitOn;
      this.removeSourceContent = removeSourceContent;
      this.groupProperties = groupProperties;
    }

    @Override
    protected void process(Text content) {
      Item item = content.getItem();

      Matcher m = splitOn.matcher(content.getData());
      int begin = 0;
      int count = 0;
      Map<String, String> properties = new HashMap<>();

      while (m.find()) {
        Content.Builder<Text, String> builder =
            item.createContent(Text.class)
                .withDescription("Split text #" + count + " from " + content.getId())
                .withData(content.getData().substring(begin, m.start()))
                .withProperty(PropertyKeys.PROPERTY_KEY_INDEX, count)
                .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, content.getId());

        for (Map.Entry<String, String> e : properties.entrySet())
          builder.withProperty(e.getKey(), e.getValue());

        builder.save();

        properties.clear();
        for (Map.Entry<Integer, String> e : groupProperties.entrySet()) {
          try {
            properties.put(e.getValue(), m.group(e.getKey()));
          } catch (IndexOutOfBoundsException ex) {
            log().warn("Could not find group {} in split pattern", e.getKey(), ex);
          }
        }

        begin = m.end();
        count++;
      }

      // Create the last block of text
      if (count > 0) {
        Content.Builder<Text, String> builder =
            item.createContent(Text.class)
                .withDescription("Split text #" + count + " from " + content.getId())
                .withData(content.getData().substring(begin))
                .withProperty(PropertyKeys.PROPERTY_KEY_INDEX, count)
                .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, content.getId());

        for (Map.Entry<String, String> e : properties.entrySet())
          builder.withProperty(e.getKey(), e.getValue());

        builder.save();
      }

      // Remove source content, but only if we've split something
      if (removeSourceContent && count > 0) item.removeContent(content);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Pattern splitOn;
    private boolean removeSourceContent;
    private Map<Integer, String> groupProperties;

    public Settings() {
      splitOn = Pattern.compile("\\*\\*\\* SEPARATOR \\*\\*\\*", Pattern.CASE_INSENSITIVE);
      removeSourceContent = false;
      groupProperties = Collections.emptyMap();
    }

    @JsonbCreator
    public Settings(
        @JsonbProperty("splitOn") Pattern splitOn,
        @JsonbProperty("removeSourceContent") boolean removeSourceContent,
        @JsonbProperty("groupProperties") Map<Integer, String> groupProperties) {
      this.splitOn = splitOn;
      this.removeSourceContent = removeSourceContent;
      this.groupProperties = groupProperties;
    }

    @Override
    public boolean validate() {
      return splitOn != null;
    }

    @Description("The pattern that text content will be split on")
    public Pattern getSplitOn() {
      return splitOn;
    }

    public void setSplitOn(Pattern splitOn) {
      this.splitOn = splitOn;
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
        "Mapping of groups in the split regex to properties - if set, the value of the group will be assigned to the specified property on the content following the split")
    public Map<Integer, String> getGroupProperties() {
      return groupProperties;
    }

    public void setGroupProperties(Map<Integer, String> groupProperties) {
      this.groupProperties = groupProperties;
    }
  }
}
