/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.annotations.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import java.util.*;
import java.util.stream.Collectors;

@ComponentName("Merge Contained Annotations")
@ComponentDescription(
    "Merge annotations with SpanBound bounds, where one is contained within the other and their types match. Properties will be merged, with subsequent properties taking precedence.")
@ComponentTags({"annotations", "merge", "cleaning"})
@SettingsClass(MergeContainedSpanBounds.Settings.class)
public class MergeContainedSpanBounds
    extends AbstractProcessorDescriptor<
        MergeContainedSpanBounds.Processor, MergeContainedSpanBounds.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    if (getSettings().getTypes().isEmpty()) {
      return new SimpleCapabilities.Builder()
          .withCreatesAnnotations("*", SpanBounds.class)
          .withDeletesAnnotations("*", SpanBounds.class)
          .build();
    } else {
      SimpleCapabilities.Builder b = new SimpleCapabilities.Builder();
      for (String s : getSettings().getTypes())
        b =
            b.withCreatesAnnotations(s, SpanBounds.class)
                .withDeletesAnnotations(s, SpanBounds.class);

      return b.build();
    }
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents()
          .forEach(
              c -> {
                Set<String> types;
                if (settings.getTypes().isEmpty()) {
                  types =
                      c.getAnnotations()
                          .getAll()
                          .map(Annotation::getType)
                          .collect(Collectors.toSet());
                } else {
                  types = settings.getTypes();
                }

                for (String type : types) {
                  List<Annotation> annotations =
                      c.getAnnotations()
                          .getByBoundsAndType(SpanBounds.class, type)
                          .sorted(
                              Comparator.comparing(
                                  a -> a.getBounds(SpanBounds.class).get().getLength()))
                          .collect(Collectors.toList());
                  if (annotations.size() <= 1) continue;

                  Map<Annotation, List<Annotation>> groups = new HashMap<>();
                  List<Annotation> grouped = new ArrayList<>();

                  for (int i = annotations.size() - 1; i >= 0; i--) {
                    Annotation a1 = annotations.get(i);
                    if (grouped.contains(a1)) continue;

                    SpanBounds s1 = a1.getBounds(SpanBounds.class).get();

                    for (int j = 0; j < i; j++) {
                      Annotation a2 = annotations.get(j);
                      SpanBounds s2 = a2.getBounds(SpanBounds.class).get();

                      if (s1.isWithin(s2)) {
                        List<Annotation> g = groups.getOrDefault(a1, new ArrayList<>());
                        g.add(a2);
                        groups.put(a1, g);

                        grouped.add(a2);
                      }
                    }
                  }

                  for (Map.Entry<Annotation, List<Annotation>> e : groups.entrySet())
                    mergeAnnotations(c, e.getKey(), e.getValue());
                }
              });

      return ProcessorResponse.ok();
    }

    private void mergeAnnotations(Content<?> c, Annotation a1, List<Annotation> a2) {
      Map<String, Object> properties = new HashMap<>();
      a2.forEach(a -> properties.putAll(a.getProperties().getAll()));
      properties.putAll(a1.getProperties().getAll());

      Annotation.Builder b = c.getAnnotations().copy(a1);

      for (Map.Entry<String, Object> e : properties.entrySet()) {
        b = b.withProperty(e.getKey(), e.getValue());
      }
      b.save();

      c.getAnnotations().delete(a1);
      c.getAnnotations().delete(a2);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Set<String> types = Collections.emptySet();

    @Description("Which types to act on - acts on all types if no types are provided")
    public Set<String> getTypes() {
      return types;
    }

    public void setTypes(Set<String> types) {
      this.types = types;
    }

    @Override
    public boolean validate() {
      return types != null;
    }
  }
}
