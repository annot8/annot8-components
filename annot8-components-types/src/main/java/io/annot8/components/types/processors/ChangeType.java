/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.types.processors;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.settings.Settings;
import io.annot8.common.components.AbstractComponent;

/**
 * Create a duplicate annotation but with a different type. The original annotation can optionally
 * be deleted or retained.
 */
public class ChangeType extends AbstractComponent implements Processor {

  private ChangeTypeSettings changeTypeSettings;

  public ChangeType(ChangeTypeSettings settings) {
    this.changeTypeSettings = settings;
  }

  @Override
  public ProcessorResponse process(Item item) {
    item.getContents()
        .forEach(
            c -> {
              c.getAnnotations()
                  .getByType(changeTypeSettings.getOriginalType())
                  .forEach(
                      a -> {
                        try {
                          if (changeTypeSettings.getRetainOriginal()) {
                            c.getAnnotations()
                                .copy(a)
                                .withType(changeTypeSettings.getNewType())
                                .save();
                          } else {
                            c.getAnnotations()
                                .edit(a)
                                .withType(changeTypeSettings.getNewType())
                                .save();
                          }
                        } catch (IncompleteException ie) {
                          log().warn("Unable to copy annotation", ie);
                        }
                      });
            });
    return ProcessorResponse.ok();
  }

  /** Configuration for the ChangeType processor */
  public static class ChangeTypeSettings extends AbstractComponent implements Settings {

    private final String originalType;
    private final String newType;
    private final boolean retainOriginal;

    public ChangeTypeSettings(String originalType, String newType) {
      this.originalType = originalType;
      this.newType = newType;
      this.retainOriginal = false;
    }

    public ChangeTypeSettings(String originalType, String newType, boolean retainOriginal) {
      this.originalType = originalType;
      this.newType = newType;
      this.retainOriginal = retainOriginal;
    }

    public String getOriginalType() {
      return originalType;
    }

    public String getNewType() {
      return newType;
    }

    public boolean getRetainOriginal() {
      return retainOriginal;
    }

    @Override
    public boolean validate() {
      return originalType != null && newType != null;
    }
  }
}
