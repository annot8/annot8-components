/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Image;
import java.util.List;
import java.util.stream.Collectors;

@ComponentName("Filter Image by Size")
@ComponentDescription("Filter Image content based on it's size")
@SettingsClass(FilterImageBySize.Settings.class)
public class FilterImageBySize
    extends AbstractProcessorDescriptor<FilterImageBySize.Processor, FilterImageBySize.Settings> {
  @Override
  protected Processor createComponent(Context context, FilterImageBySize.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withDeletesContent(Image.class).build();
  }

  public static class Processor extends AbstractProcessor {

    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Image> toRemove =
          item.getContents(Image.class)
              .filter(
                  i -> {
                    if (i.getWidth() < settings.getMinWidth()
                        || i.getHeight() < settings.getMinHeight()) return true;

                    if (settings.getMaxWidth() > 0 && i.getWidth() > settings.getMaxWidth())
                      return true;

                    if (settings.getMaxHeight() > 0 && i.getHeight() > settings.getMaxHeight())
                      return true;

                    return false;
                  })
              .collect(Collectors.toList());

      for (Image i : toRemove) item.removeContent(i);

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private int minWidth = 0;
    private int minHeight = 0;
    private int maxWidth = -1;
    private int maxHeight = -1;

    @Override
    public boolean validate() {
      return true;
    }

    @Description(
        value = "Images with a width smaller than this will be discarded",
        defaultValue = "0")
    public int getMinWidth() {
      return minWidth;
    }

    public void setMinWidth(int minWidth) {
      this.minWidth = minWidth;
    }

    @Description(
        value = "Images with a height smaller than this will be discarded",
        defaultValue = "0")
    public int getMinHeight() {
      return minHeight;
    }

    public void setMinHeight(int minHeight) {
      this.minHeight = minHeight;
    }

    @Description(
        value =
            "Images with a width greater than this will be discarded. If negative, then no maximum width will apply.",
        defaultValue = "-1")
    public int getMaxWidth() {
      return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
      this.maxWidth = maxWidth;
    }

    @Description(
        value =
            "Images with a height greater than this will be discarded. If negative, then no maximum height will apply.",
        defaultValue = "-1")
    public int getMaxHeight() {
      return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
      this.maxHeight = maxHeight;
    }
  }
}
