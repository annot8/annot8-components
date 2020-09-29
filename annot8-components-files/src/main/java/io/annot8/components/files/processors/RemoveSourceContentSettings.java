/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;

public class RemoveSourceContentSettings implements Settings {
  private boolean removeSourceContent = true;

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
}
