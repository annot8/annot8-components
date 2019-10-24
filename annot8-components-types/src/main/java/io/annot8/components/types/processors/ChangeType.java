/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.types.processors;

import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 * Create a duplicate annotation but with a different type. The original annotation can optionally
 * be deleted or retained.
 */
@ComponentName("Change Type")
@ComponentDescription("Change the type of an annotation")
@SettingsClass(ChangeType.Settings.class)
public class ChangeType
    extends AbstractProcessorDescriptor<ChangeType.Processor, ChangeType.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(
        settings.getOriginalType(), settings.getNewType(), settings.getRetainOriginal());
  }

  @Override
  public Capabilities capabilities() {

    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesAnnotations(getSettings().getOriginalType(), Bounds.class)
            .withCreatesAnnotations(getSettings().getNewType(), Bounds.class);

    if (!getSettings().getRetainOriginal())
      builder = builder.withDeletesAnnotations(getSettings().getOriginalType(), Bounds.class);

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {

    private final String originalType;
    private final String newType;
    private final boolean retainOriginal;

    public Processor(String originalType, String newType, boolean retainOriginal) {
      this.originalType = originalType;
      this.newType = newType;
      this.retainOriginal = retainOriginal;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents()
          .forEach(
              c -> {
                c.getAnnotations()
                    .getByType(originalType)
                    .forEach(
                        a -> {
                          try {
                            if (retainOriginal) {
                              c.getAnnotations().copy(a).withType(newType).save();
                            } else {
                              c.getAnnotations().edit(a).withType(newType).save();
                            }
                          } catch (IncompleteException ie) {
                            log().warn("Unable to copy annotation", ie);
                          }
                        });
              });
      return ProcessorResponse.ok();
    }
  }

  /** Configuration for the ChangeType processor */
  public static class Settings implements io.annot8.api.settings.Settings {

    private final String originalType;
    private final String newType;
    private final boolean retainOriginal;

    public Settings(String originalType, String newType) {
      this.originalType = originalType;
      this.newType = newType;
      this.retainOriginal = false;
    }

    @JsonbCreator
    public Settings(
        @JsonbProperty("originalType") String originalType,
        @JsonbProperty("newType") String newType,
        @JsonbProperty("retainOriginal") boolean retainOriginal) {
      this.originalType = originalType;
      this.newType = newType;
      this.retainOriginal = retainOriginal;
    }

    @Description("The original type to change")
    public String getOriginalType() {
      return originalType;
    }

    @Description("The type to change to")
    public String getNewType() {
      return newType;
    }

    @Description("Whether the original annotation should be retained (true) or removed (false)")
    public boolean getRetainOriginal() {
      return retainOriginal;
    }

    @Override
    public boolean validate() {
      return originalType != null && newType != null;
    }
  }
}
