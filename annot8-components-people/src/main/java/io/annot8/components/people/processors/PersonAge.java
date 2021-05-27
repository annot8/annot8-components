/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.people.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Person Age")
@ComponentDescription(
    "Identify the age of a person where it is stated following a Person annotation (e.g. \"John (32 y/o)\")")
public class PersonAge extends AbstractProcessorDescriptor<PersonAge.Processor, NoSettings> {
  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PERSON, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private static final Pattern AGE =
        Pattern.compile(
            "(, | \\()(?<age>\\d+) ?(yo|yrs?|y.o.|y[/\\\\]o|years?|years? old)?[.,)]",
            Pattern.CASE_INSENSITIVE);

    @Override
    protected void process(Text content) {
      content
          .getAnnotations()
          .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_PERSON)
          .forEach(
              p -> {
                SpanBounds sb = p.getBounds(SpanBounds.class).orElse(null);
                if (sb == null) return;

                int age = -1;

                String followingText = content.getData().substring(sb.getEnd());
                Matcher m = AGE.matcher(followingText);
                if (m.find() && m.start() == 0) {
                  age = Integer.parseInt(m.group("age"));
                } else {
                  // Match not found, check whether it's been mistakenly included within the Person
                  // annotation
                  Matcher mWithin =
                      AGE.matcher(content.getData().substring(sb.getBegin(), sb.getEnd()));
                  if (mWithin.find() && mWithin.end() == sb.getLength()) {
                    age = Integer.parseInt(mWithin.group("age"));

                    sb = new SpanBounds(sb.getBegin(), sb.getBegin() + mWithin.start());
                  }
                }

                if (age == -1) return;

                content
                    .getAnnotations()
                    .create()
                    .from(p)
                    .withBounds(sb)
                    .withProperty(PropertyKeys.PROPERTY_KEY_AGE, age)
                    .save();
              });
    }
  }
}
