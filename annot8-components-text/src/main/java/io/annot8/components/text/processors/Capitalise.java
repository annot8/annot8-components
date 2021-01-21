/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static io.annot8.components.text.processors.Capitalise.TextCase.UPPERCASE;

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
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

@ComponentName("Capitalise")
@ComponentDescription("Capitalise or lower case text")
@SettingsClass(Capitalise.Settings.class)
public class Capitalise
    extends AbstractProcessorDescriptor<Capitalise.Processor, Capitalise.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getTextCase(), settings.isRemoveSourceContent());
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

    private final TextCase textCase;
    private final boolean removeSourceContent;

    public Processor(TextCase textCase, boolean removeSourceContent) {
      this.textCase = textCase;
      this.removeSourceContent = removeSourceContent;
    }

    @Override
    protected void process(Text content) {
      Item item = content.getItem();

      switch (textCase) {
        case UPPERCASE:
          item.createContent(Text.class)
              .withDescription(String.format("Upper cased content[%s]", content.getId()))
              .withData(content.getData().toUpperCase())
              .save();
          break;
        case LOWERCASE:
          item.createContent(Text.class)
              .withDescription(String.format("Lower cased content[%s]", content.getId()))
              .withData(content.getData().toLowerCase())
              .save();
          break;
      }

      if (removeSourceContent) item.removeContent(content);
    }
  }

  public enum TextCase {
    UPPERCASE,
    LOWERCASE
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private TextCase textCase;
    private boolean removeSourceContent;

    public Settings() {
      textCase = UPPERCASE;
      removeSourceContent = false;
    }

    @JsonbCreator
    public Settings(
        @JsonbProperty("textCase") TextCase textCase,
        @JsonbProperty("removeSourceContent") boolean removeSourceContent) {
      this.textCase = textCase;
      this.removeSourceContent = removeSourceContent;
    }

    @Override
    public boolean validate() {
      return true;
    }

    @Description("What case the text should be normalized to")
    public TextCase getTextCase() {
      return textCase;
    }

    public void setTextCase(TextCase textCase) {
      this.textCase = textCase;
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
