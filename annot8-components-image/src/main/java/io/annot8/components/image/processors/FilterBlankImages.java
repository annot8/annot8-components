/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Image;
import java.util.List;
import java.util.stream.Collectors;

@ComponentName("Filter Blank Images")
@ComponentDescription("Filter images where all pixels are the same colour")
@ComponentTags({"image", "filter"})
public class FilterBlankImages
    extends AbstractProcessorDescriptor<FilterBlankImages.Processor, NoSettings> {
  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withDeletesContent(Image.class).build();
  }

  public static class Processor extends AbstractProcessor {

    @Override
    public ProcessorResponse process(Item item) {
      List<Image> toRemove =
          item.getContents(Image.class)
              .filter(
                  i -> {
                    int colour = i.getData().getRGB(0, 0);

                    for (int x = 0; x < i.getWidth(); x++) {
                      for (int y = 0; y < i.getHeight(); y++) {
                        if (colour != i.getData().getRGB(x, y)) return false;
                      }
                    }

                    return true;
                  })
              .collect(Collectors.toList());

      for (Image i : toRemove) item.removeContent(i);

      return ProcessorResponse.ok();
    }
  }
}
