/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.properties.Properties;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;

@ComponentName("Print Items")
@ComponentDescription("Prints information about each item")
@SettingsClass(PrintSettings.class)
public class PrintItem extends AbstractProcessorDescriptor<PrintItem.Processor, PrintSettings> {

  @Override
  protected Processor createComponent(Context context, PrintSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withProcessesAnnotations("*", Bounds.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    private final PrintSettings settings;

    public Processor(PrintSettings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {

      println("---", 0);
      println("Properties:", 0);
      print(item.getProperties(), 1);
      println("Content:", 0);
      item.getContents().forEach(this::print);

      return ProcessorResponse.ok();
    }

    private void print(Content<?> content) {
      println(content.getId() + " [" + content.getClass().getName() + "]", 1);
      println("Properties:", 2);
      print(content.getProperties(), 3);
      println("Annotations:", 2);
      print(content.getAnnotations(), 3);
    }

    private void print(Properties properties, int indent) {
      properties
          .getAll()
          .forEach((key, value) -> println(String.format("%s: %s", key, value), indent));
    }

    private void print(AnnotationStore annotations, int indent) {
      annotations.getAll().forEach(a -> print(a, indent));
    }

    private void print(Annotation annotation, int indent) {
      println(annotation.toString(), indent);
    }

    private void println(String s, int indent) {
      String sb = " ".repeat(Math.max(0, indent)) + s;

      settings.output(log(), sb);
    }
  }
}
