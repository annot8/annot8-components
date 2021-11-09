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
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Split Annotations")
@ComponentDescription("Split annotations that span a given character in TextContent")
@ComponentTags({"annotations", "split", "cleaning"})
@SettingsClass(SplitSpanBounds.Settings.class)
public class SplitSpanBounds
    extends AbstractProcessorDescriptor<SplitSpanBounds.Processor, SplitSpanBounds.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    if (getSettings().getTypes().isEmpty()) {
      return new SimpleCapabilities.Builder()
          .withProcessesContent(Text.class)
          .withCreatesAnnotations("*", SpanBounds.class)
          .withDeletesAnnotations("*", SpanBounds.class)
          .build();
    } else {
      SimpleCapabilities.Builder b =
          new SimpleCapabilities.Builder().withProcessesContent(Text.class);

      for (String s : getSettings().getTypes())
        b =
            b.withCreatesAnnotations(s, SpanBounds.class)
                .withDeletesAnnotations(s, SpanBounds.class);

      return b.build();
    }
  }

  public static class Processor extends AbstractProcessor {
    private final Set<String> types;
    private final Pattern splitPattern;

    public Processor(Settings settings) {
      this.types = settings.getTypes();
      this.splitPattern = Pattern.compile(settings.getSplit());
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents(Text.class)
          .forEach(
              c -> {
                List<Annotation> toRemove = new ArrayList<>();

                c.getAnnotations()
                    .getByBounds(SpanBounds.class)
                    .filter(
                        a -> {
                          if (types.isEmpty()) {
                            return true;
                          } else {
                            return types.contains(a.getType());
                          }
                        })
                    .forEach(
                        a -> {
                          SpanBounds sb =
                              a.getBounds(SpanBounds.class).orElse(new SpanBounds(0, 0));
                          String s = sb.getData(c).orElse("");

                          Matcher m = splitPattern.matcher(s);

                          int lastIdx = 0;
                          int count = 0;

                          while (m.find()) {
                            count++;

                            c.getAnnotations()
                                .create()
                                .withType(a.getType())
                                .withBounds(
                                    new SpanBounds(
                                        sb.getBegin() + lastIdx, sb.getBegin() + m.start()))
                                .withProperties(a.getProperties())
                                .save();

                            lastIdx = m.end();
                          }

                          if (count > 0) {
                            c.getAnnotations()
                                .create()
                                .withType(a.getType())
                                .withBounds(new SpanBounds(sb.getBegin() + lastIdx, sb.getEnd()))
                                .withProperties(a.getProperties())
                                .save();

                            log()
                                .debug(
                                    "Split annotation {} into {} annotations",
                                    a.getId(),
                                    count + 1);
                            toRemove.add(a);
                          }
                        });

                if (!toRemove.isEmpty()) {
                  log().info("{} annotations split in content {}", toRemove.size(), c.getId());
                  c.getAnnotations().delete(toRemove);
                }
              });

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Set<String> types = Collections.emptySet();
    private String split = "\n";

    @Description("Which types to act on - acts on all types if no types are provided")
    public Set<String> getTypes() {
      return types;
    }

    public void setTypes(Set<String> types) {
      this.types = types;
    }

    @Description("The regular expression to split on within the annotation span")
    public String getSplit() {
      return split;
    }

    public void setSplit(String split) {
      this.split = split;
    }

    @Override
    public boolean validate() {
      return types != null && split != null && !split.isEmpty();
    }
  }
}
