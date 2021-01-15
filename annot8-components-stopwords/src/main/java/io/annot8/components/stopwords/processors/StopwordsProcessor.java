/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.stopwords.resources.Stopwords;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StopwordsProcessor extends AbstractProcessor {
  private final Stopwords stopwords;
  private final List<String> types;

  public StopwordsProcessor(Stopwords stopwords, List<String> types) {
    this.stopwords = stopwords;

    if (types == null) {
      this.types = List.of();
    } else {
      this.types = types;
    }
  }

  @Override
  public ProcessorResponse process(Item item) {
    item.getContents(Text.class)
        .forEach(
            c -> {
              Stream<Annotation> annotations;
              if (types.isEmpty()) {
                annotations = c.getAnnotations().getByBounds(SpanBounds.class);
              } else {
                annotations =
                    types.stream()
                        .flatMap(t -> c.getAnnotations().getByBoundsAndType(SpanBounds.class, t));
              }

              List<Annotation> toRemove =
                  annotations
                      .filter(
                          a ->
                              stopwords.isStopword(
                                  a.getBounds(SpanBounds.class).get().getData(c).orElse("")))
                      .collect(Collectors.toList());

              if (!toRemove.isEmpty()) {
                c.getAnnotations().delete(toRemove);
              }
            });
    return ProcessorResponse.ok();
  }
}
