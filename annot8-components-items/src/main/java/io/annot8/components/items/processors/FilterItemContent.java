/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ComponentName("Filter Item Content")
@ComponentDescription("Filter Content of a given type")
@SettingsClass(FilterItemContent.Settings.class)
@ComponentTags({"item", "content", "filter"})
public class FilterItemContent
    extends AbstractProcessorDescriptor<FilterItemContent.Processor, FilterItemContent.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getContentClasses(), settings.isDiscardMatching());
  }

  public static class Processor extends AbstractProcessor {
    private final List<Class<Content<?>>> contentClasses = new ArrayList<>();
    private final boolean discardMatching;

    public Processor(List<String> contentClasses, boolean discardMatching) {
      contentClasses.forEach(
          s -> {
            try {
              Class<?> c = Class.forName(s);

              if (Content.class.isAssignableFrom(c)) {
                this.contentClasses.add((Class<Content<?>>) c);
              } else {
                log().warn("Class {} is not a sub-class of Content", s);
              }
            } catch (ClassNotFoundException e) {
              log().warn("Class {} could not be found", s);
            }
          });
      this.discardMatching = discardMatching;
    }

    @Override
    public ProcessorResponse process(Item item) {
      Set<Content<?>> matching =
          contentClasses.stream().flatMap(item::getContents).collect(Collectors.toSet());

      if (discardMatching) {
        log().info("Discarding {} Contents that match one of the given classes", matching.size());
        matching.forEach(item::removeContent);
      } else {
        List<Content<?>> nonMatching =
            item.getContents().filter(c -> !matching.contains(c)).collect(Collectors.toList());

        log()
            .info(
                "Discarding {} Contents that don't match any of the given classes",
                nonMatching.size());
        nonMatching.forEach(item::removeContent);
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> contentClasses = new ArrayList<>();
    private boolean discardMatching = true;

    @Override
    public boolean validate() {
      return contentClasses != null && !contentClasses.isEmpty();
    }

    @Description("A list of Content class names to filter by")
    public List<String> getContentClasses() {
      return contentClasses;
    }

    public void setContentClasses(List<String> contentClasses) {
      this.contentClasses = contentClasses;
    }

    @Description(
        "If true, then matching Content is discarded. Otherwise, non-matching Content are discarded.")
    public boolean isDiscardMatching() {
      return discardMatching;
    }

    public void setDiscardMatching(boolean discardMatching) {
      this.discardMatching = discardMatching;
    }
  }
}
