/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import java.util.Optional;

@ComponentName("Print Spans")
@ComponentDescription("Prints information about each item")
@SettingsClass(PrintSettings.class)
public class PrintSpans extends AbstractProcessorDescriptor<PrintSpans.Processor, PrintSettings> {

  @Override
  protected Processor createComponent(Context context, PrintSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations("*", SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private final PrintSettings settings;

    public Processor(PrintSettings settings) {
      this.settings = settings;
    }

    @Override
    protected void process(final Text content) {
      content
          .getAnnotations()
          .getByBounds(SpanBounds.class)
          .forEach(
              a -> {
                final SpanBounds bounds = a.getBounds(SpanBounds.class).get();
                final Optional<String> value = content.getText(a);

                settings.output(
                    log(),
                    String.format("Annotation %s: %s", bounds.toString(), value.orElse("UNKNOWN")));
              });
    }
  }
}
