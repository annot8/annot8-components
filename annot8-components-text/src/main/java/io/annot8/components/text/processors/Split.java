/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

@ComponentName("Split Text")
@ComponentDescription("Split Text content into separate contents")
@SettingsClass(Split.Settings.class)
public class Split extends AbstractProcessorDescriptor<Split.Processor, Split.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getSplitOn(), settings.isRemoveSourceContent());
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

    public Processor(Pattern splitOn, boolean removeSourceContent) {
      this.splitOn = splitOn;
      this.removeSourceContent = removeSourceContent;
    }

    @Override
    protected void process(Text content) {
      Item item = content.getItem();

      Matcher m = splitOn.matcher(content.getData());
      int begin = 0;
      int count = 0;
      while (m.find()) {
        item.createContent(Text.class)
            .withDescription("Split text #" + count + " from " + content.getId())
            .withData(content.getData().substring(begin, m.start()))
            .save();

        begin = m.end();
        count++;
      }

      // Create the last block of text
      if (count > 0)
        item.createContent(Text.class)
            .withDescription("Split text #" + count + " from " + content.getId())
            .withData(content.getData().substring(begin))
            .save();

      // Remove source content, but only if we've split something
      if (removeSourceContent && count > 0) item.removeContent(content);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Pattern splitOn;
    private boolean removeSourceContent;

    public Settings() {
      splitOn = Pattern.compile("\\*\\*\\* SEPARATOR \\*\\*\\*", Pattern.CASE_INSENSITIVE);
      removeSourceContent = false;
    }

    @JsonbCreator
    public Settings(
        @JsonbProperty("splitOn") Pattern splitOn,
        @JsonbProperty("removeSourceContent") boolean removeSourceContent) {
      this.splitOn = splitOn;
      this.removeSourceContent = removeSourceContent;
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
  }
}
