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
import java.util.regex.Pattern;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

@ComponentName("Discard Text")
@ComponentDescription("Discard text content if it matches a regular expression")
@SettingsClass(Discard.Settings.class)
public class Discard extends AbstractProcessorDescriptor<Discard.Processor, Discard.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getDiscardOn(), settings.isInverse());
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder().withDeletesContent(Text.class);

    return builder.build();
  }

  public static class Processor extends AbstractTextProcessor {

    private final Pattern discardOn;
    private final boolean inverse;

    public Processor(Pattern discardOn, boolean inverse) {
      this.discardOn = discardOn;
      this.inverse = inverse;
    }

    @Override
    protected void process(Text content) {
      Item item = content.getItem();

      if (discardOn.matcher(content.getData()).matches() ^ inverse) {
        item.removeContent(content);
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Pattern discardOn;
    private boolean inverse;

    public Settings() {
      discardOn = Pattern.compile(".*discard me.*", Pattern.CASE_INSENSITIVE);
      inverse = false;
    }

    @JsonbCreator
    public Settings(
        @JsonbProperty("discardOn") Pattern discardOn, @JsonbProperty("inverse") boolean inverse) {
      this.discardOn = discardOn;
      this.inverse = inverse;
    }

    @Override
    public boolean validate() {
      return discardOn != null;
    }

    @Description("The pattern to match to determine whether text should be discarded")
    public Pattern getDiscardOn() {
      return discardOn;
    }

    public void setDiscardOn(Pattern discardOn) {
      this.discardOn = discardOn;
    }

    @Description(
        "If true, then text that doesn't match will be discarded (rather than text that does match)")
    public boolean isInverse() {
      return inverse;
    }

    public void setInverse(boolean inverse) {
      this.inverse = inverse;
    }
  }
}
