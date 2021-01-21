/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.grouping.processors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
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
import io.annot8.api.stores.GroupStore;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import java.util.Objects;
import java.util.Optional;

@ComponentName("Group by Type and Value")
@ComponentDescription("Group annotations within a Content where their type and value are the same")
public class GroupByTypeAndValue
    extends AbstractProcessorDescriptor<GroupByTypeAndValue.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new GroupByTypeAndValue.Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesAnnotations("*", SpanBounds.class)
        .withCreatesGroups(Processor.TYPE)
        .build();
  }

  public static class Processor extends AbstractProcessor {

    public static final String TYPE = "exactMatches";
    private static final String ROLE = "as";

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents().forEach(this::process);
      return ProcessorResponse.ok();
    }

    protected <D> void process(Content<D> content) {

      SetMultimap<TypeObjectPair, Annotation> map = HashMultimap.create();

      // Collate up all the annotations which have the same type

      content
          .getAnnotations()
          .getByBounds(SpanBounds.class)
          .forEach(
              a -> {
                Optional<D> optional = a.getBounds().getData(content);
                optional.ifPresent(covered -> map.put(new TypeObjectPair(a.getType(), covered), a));
              });

      // Create a group for things which are the same
      GroupStore groupStore = content.getItem().getGroups();
      map.asMap().values().stream()
          .filter(annotations -> annotations.size() > 1)
          .forEach(
              annotations -> {
                Group.Builder builder = groupStore.create().withType(TYPE);
                annotations.forEach(a -> builder.withAnnotation(ROLE, a));
                builder.save();
              });
    }

    private class TypeObjectPair {
      private final String type;
      private final Object object;

      public TypeObjectPair(String type, Object object) {
        this.type = type;
        this.object = object;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeObjectPair that = (TypeObjectPair) o;
        return Objects.equals(type, that.type) && Objects.equals(object, that.object);
      }

      @Override
      public int hashCode() {
        return Objects.hash(type, object);
      }
    }
  }
}
