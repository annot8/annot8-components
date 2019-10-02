/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import java.util.Optional;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;

@ComponentName("Print Spans")
@ComponentDescription("Prints information about each item")
public class PrintSpans extends AbstractProcessorDescriptor<PrintSpans.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations("*", SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    @Override
    protected void process(final Text content) {

      content
          .getAnnotations()
          .getByBounds(SpanBounds.class)
          .forEach(
              a -> {
                final SpanBounds bounds = a.getBounds(SpanBounds.class).get();
                final Optional<String> value = content.getText(a);

                // TODO: Log rather than print if Logging preset
                System.out.println(
                    String.format("Annotation %s: %s", bounds.toString(), value.orElse("UNKNOWN")));
              });
    }
  }
}
