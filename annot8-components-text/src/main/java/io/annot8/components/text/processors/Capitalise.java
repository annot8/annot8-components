/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static io.annot8.components.text.processors.Capitalise.TextCase.UPPERCASE;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.components.text.processors.Capitalise.CapitaliseSettings;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.Settings;
import io.annot8.core.settings.SettingsClass;

@CreatesContent(Text.class)
@SettingsClass(CapitaliseSettings.class)
public class Capitalise extends AbstractTextProcessor {

  private CapitaliseSettings settings = new CapitaliseSettings();

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    settings = context.getSettings(CapitaliseSettings.class).orElse(new CapitaliseSettings());
  }

  @Override
  protected void process(Item item, Text content) throws Annot8Exception {
    switch (settings.getTextCase()) {
      case UPPERCASE:
        item.create(Text.class)
            .withName(content.getName() + "_upper")
            .withData(content.getData().toUpperCase())
            .save();
        break;
      case LOWERCASE:
        item.create(Text.class)
            .withName(content.getName() + "_lower")
            .withData(content.getData().toLowerCase())
            .save();
        break;
    }
  }

  public enum TextCase {
    UPPERCASE,
    LOWERCASE
  }

  public static class CapitaliseSettings implements Settings {
    private TextCase textCase;

    public CapitaliseSettings() {
      textCase = UPPERCASE;
    }

    public CapitaliseSettings(TextCase textCase) {
      this.textCase = textCase;
    }

    @Override
    public boolean validate() {
      return true;
    }

    public TextCase getTextCase() {
      return textCase;
    }

    public void setTextCase(TextCase textCase) {
      this.textCase = textCase;
    }
  }
}
