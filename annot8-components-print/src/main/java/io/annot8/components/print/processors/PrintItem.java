/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.properties.Properties;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.components.AbstractProcessor;

public class PrintItem extends AbstractProcessor {

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
    println(content.getId(), 1);
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
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      sb.append(" ");
    }
    sb.append(s);

    System.out.println(sb.toString());
  }
}
