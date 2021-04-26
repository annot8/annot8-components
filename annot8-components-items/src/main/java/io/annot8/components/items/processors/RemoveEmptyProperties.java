/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

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
import java.util.List;
import java.util.stream.Collectors;

@ComponentName("Remove Empty Properties")
@ComponentDescription("Remove Item Properties that are empty or blank")
@ComponentTags({"item", "properties"})
public class RemoveEmptyProperties
    extends AbstractProcessorDescriptor<RemoveEmptyProperties.Processor, NoSettings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  public static class Processor extends AbstractProcessor {
    @Override
    public ProcessorResponse process(Item item) {

      List<String> emptyKeys =
          item.getProperties()
              .keys()
              .filter(
                  key -> {
                    Object o = item.getProperties().get(key).orElse(null);

                    if (o == null) return true;

                    if (o instanceof String) {
                      String s = (String) o;
                      if (s.isBlank()) return true;
                    }

                    return false;
                  })
              .collect(Collectors.toList());

      item.getProperties().remove(emptyKeys);

      return ProcessorResponse.ok();
    }
  }
}
