/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.components.base.utils.TypeUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupTypes;
import java.util.List;
import java.util.stream.Collectors;

@ComponentName("Filter Grammar Annotations and Groups")
@ComponentDescription("Remove all grammar annotations and groups (except coreference) from Content")
public class FilterGrammar
    extends AbstractProcessorDescriptor<FilterGrammar.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withDeletesGroups(GroupTypes.GRAMMAR_PREFIX + "*")
        .withDeletesAnnotations(AnnotationTypes.GRAMMAR_PREFIX + "*", SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    @Override
    public ProcessorResponse process(Item item) {
      item.getContents()
          .forEach(
              c -> {
                List<Annotation> toRemove =
                    c.getAnnotations()
                        .getAll()
                        .filter(
                            a ->
                                TypeUtils.matchesWildcard(
                                    a.getType(), AnnotationTypes.GRAMMAR_PREFIX + "*"))
                        .collect(Collectors.toList());

                log()
                    .info(
                        "Removing {} grammar annotations from Content {}",
                        toRemove.size(),
                        c.getDescription());

                c.getAnnotations().delete(toRemove);
              });

      List<Group> toRemove =
          item.getGroups()
              .getAll()
              .filter(g -> TypeUtils.matchesWildcard(g.getType(), GroupTypes.GRAMMAR_PREFIX + "*"))
              .filter(g -> !g.getType().equals(GroupTypes.GROUP_TYPE_GRAMMAR_COREFERENCE))
              .collect(Collectors.toList());

      log().info("Removing {} grammar groups from Item {}", toRemove.size(), item.getId());

      item.getGroups().delete(toRemove);

      return ProcessorResponse.ok();
    }
  }
}
