/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;

@ComponentName("Discard Unextracted Files")
@ComponentDescription("Discards FileContent which hasn't been extracted into other Content")
public class DiscardUnextractedFiles extends AbstractProcessorDescriptor<DiscardUnextractedFiles.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(FileContent.class)
        .withDeletesContent(FileContent.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {

    @Override
    public ProcessorResponse process(Item item) {

      item.getContents(FileContent.class).forEach(item::removeContent);

      boolean noOtherContent = item.getContents().count() == 0;

      if (noOtherContent) {
        item.discard();
      }

      return ProcessorResponse.ok();
    }
  }
}
