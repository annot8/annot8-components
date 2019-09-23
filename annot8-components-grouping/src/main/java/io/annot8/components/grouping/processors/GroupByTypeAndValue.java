/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.grouping.processors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.GroupStore;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;

import java.util.Optional;

@ComponentName("Group by Type and Value")
@ComponentDescription("Group annotations where their type and covered text (value) are the same")
public class GroupByTypeAndValue extends AbstractProcessorDescriptor<GroupByTypeAndValue.Processor, NoSettings> {

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

  public static class Processor extends AbstractTextProcessor {

    public static final String TYPE = "exactMatches";
    private static final String ROLE = "as";

    @Override
    protected void process(Text content) {

      SetMultimap<String, Annotation> map = HashMultimap.create();

      // Collate up all the annotations which have the same type

      content
          .getAnnotations()
          .getByBounds(SpanBounds.class)
          .forEach(
              a -> {
                Optional<String> optional = content.getText(a);
                optional.ifPresent(
                    covered -> {
                      String key = toKey(a.getType(), covered);
                      map.put(key, a);
                    });
              });

      // Create a group for things which are the same
      GroupStore groupStore = content.getItem().getGroups();
      map.asMap()
          .values()
          .forEach(
              annotations -> {
                Group.Builder builder = groupStore.create().withType(TYPE);
                annotations.forEach(a -> builder.withAnnotation(ROLE, a));
                builder.save();
              });
    }

    private String toKey(String type, String covered) {
      return type + ":" + covered;
    }
  }
}
