/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.types.processors;

import java.util.Optional;

import io.annot8.components.base.components.AbstractComponent;
import io.annot8.components.types.processors.ChangeType.ChangeTypeSettings;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.Settings;
import io.annot8.core.settings.SettingsClass;

/**
 * Create a duplicate annotation but with a different type. The original annotation can optionally
 * be deleted or retained.
 */
@SettingsClass(ChangeTypeSettings.class)
public class ChangeType extends AbstractComponent implements Processor {

  private ChangeTypeSettings changeTypeSettings = null;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    Optional<ChangeTypeSettings> opt = context.getSettings(ChangeTypeSettings.class);
    if (!opt.isPresent()) {
      throw new BadConfigurationException("Must provide a ChangeTypeSettings object");
    }

    this.changeTypeSettings = opt.get();
  }

  @Override
  public ProcessorResponse process(Item item) throws Annot8Exception {
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
