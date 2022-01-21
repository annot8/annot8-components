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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ComponentName("Merge Adjacent Annotations")
@ComponentDescription(
    "Merge annotations with SpanBound bounds, where their bounds are adjacent and their types match. Properties will be merged, with subsequent properties taking precedence.")
@ComponentTags({"annotations", "merge", "cleaning"})
@SettingsClass(MergeAdjacentSpanBounds.Settings.class)
public class MergeAdjacentSpanBounds
    extends AbstractProcessorDescriptor<
        MergeAdjacentSpanBounds.Processor, MergeAdjacentSpanBounds.Settings> {

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
                                  a -> a.getBounds(SpanBounds.class).get().getBegin()))
                          .collect(Collectors.toList());

                  if (annotations.size() < 2) continue;

                  Annotation aCurr = annotations.get(0);
                  for (int i = 1; i < annotations.size(); i++) {
                    Annotation a2 = annotations.get(i);

                    SpanBounds s1 = aCurr.getBounds(SpanBounds.class).get();
                    SpanBounds s2 = a2.getBounds(SpanBounds.class).get();

                    boolean merge = true;

                    if (!s2.isOverlaps(s1)) {
                      if (settings.getMaxRepeatableSeparators() < s2.getBegin() - s1.getBegin()
                          && settings.getMaxRepeatableSeparators() >= 0) merge = false;

                      for (int j = s1.getEnd(); j < s2.getBegin() && merge; j++) {
                        Optional<?> dataStep = new SpanBounds(j, j + 1).getData(c);

                        merge =
                            dataStep
                                .filter(o -> settings.getAllowableSeparators().contains(o))
                                .isPresent();
                      }
                    }

                    if (merge) {
                      aCurr = mergeAnnotations(c, aCurr, a2);
                    } else {
                      aCurr = a2;
                    }
                  }
                }
              });

      return ProcessorResponse.ok();
    }

    private Annotation mergeAnnotations(Content<?> c, Annotation a1, Annotation a2) {
      SpanBounds s1 = a1.getBounds(SpanBounds.class).get();
      SpanBounds s2 = a2.getBounds(SpanBounds.class).get();
      SpanBounds s = new SpanBounds(s1.getBegin(), s2.getEnd());

      Map<String, Object> properties = new HashMap<>(a1.getProperties().getAll());
      properties.putAll(a2.getProperties().getAll());

      Annotation.Builder a = c.getAnnotations().create().withType(a1.getType()).withBounds(s);

      for (Map.Entry<String, Object> e : properties.entrySet()) {
        a = a.withProperty(e.getKey(), e.getValue());
      }

      c.getAnnotations().delete(List.of(a1, a2));

      return a.save();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Set<String> types = Collections.emptySet();
    private Set<Object> allowableSeparators = Set.of(" ");
    private int maxRepeatableSeparators = -1;

    @Description("Which types to act on - acts on all types if no types are provided")
    public Set<String> getTypes() {
      return types;
    }

    public void setTypes(Set<String> types) {
      this.types = types;
    }

    @Description("Which separators, if any, are allowed between annotations")
    public Set<Object> getAllowableSeparators() {
      return allowableSeparators;
    }

    public void setAllowableSeparators(Set<Object> allowableSeparators) {
      this.allowableSeparators = allowableSeparators;
    }

    @Description("The maximum number of separators allowed between annotations (-1 for no limit)")
    public int getMaxRepeatableSeparators() {
      return maxRepeatableSeparators;
    }

    public void setMaxRepeatableSeparators(int maxRepeatableSeparators) {
      this.maxRepeatableSeparators = maxRepeatableSeparators;
    }

    @Override
    public boolean validate() {
      return types != null && allowableSeparators != null;
    }
  }
}
