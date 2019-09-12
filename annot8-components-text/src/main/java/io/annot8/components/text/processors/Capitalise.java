/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static io.annot8.components.text.processors.Capitalise.TextCase.UPPERCASE;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.core.data.Item;
import io.annot8.core.settings.Settings;

public class Capitalise extends AbstractTextProcessor {

  private CapitaliseSettings settings = new CapitaliseSettings();

  public Capitalise(CapitaliseSettings settings) {
    this.settings = settings;
  }

  @Override
  protected void process(Text content) {
    Item item = content.getItem();

    switch (settings.getTextCase()) {
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
