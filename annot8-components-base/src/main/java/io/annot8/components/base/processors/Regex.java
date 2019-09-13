/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.context.Context;

public class Regex extends AbstractProcessorDescriptor<RegexProcessor, RegexSettings> {
  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(getSettings().getType(), SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  public RegexProcessor createComponent(Context context, RegexSettings settings) {
    return new RegexProcessor(settings);
  }
}
