/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.people.processors;

import io.annot8.api.annotations.Annotation;
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
import java.util.List;

@ComponentName("Singular Pronouns")
@ComponentDescription("Extracts pronouns (e.g. I, she) as people")
public class SingularPronouns
    extends AbstractProcessorDescriptor<SingularPronouns.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PERSON, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private static List<String> femalePronouns = List.of("she", "hers", "her", "herself");
    private static List<String> malePronouns = List.of("he", "his", "him", "himself");
    private static List<String> ambiguousPronouns =
        List.of("you", "yourself", "me", "my", "myself");

    @Override
    protected void process(Text content) {
      content
          .getAnnotations()
          .getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
          .forEach(
              a -> {
                String word =
                    a.getBounds(SpanBounds.class)
                        .orElse(new SpanBounds(0, 0))
                        .getData(content)
                        .orElse("");

                if (word.isBlank()) return;

                String wordLower = word.toLowerCase();

                Annotation.Builder b = null;
                if (word.equals("I") || ambiguousPronouns.contains(wordLower)) {
                  b = content.getAnnotations().create();
                } else if (femalePronouns.contains(wordLower)) {
                  b =
                      content
                          .getAnnotations()
                          .create()
                          .withProperty(PropertyKeys.PROPERTY_KEY_GENDER, "female");
                } else if (malePronouns.contains(wordLower)) {
                  b =
                      content
                          .getAnnotations()
                          .create()
                          .withProperty(PropertyKeys.PROPERTY_KEY_GENDER, "male");
                }

                if (b == null) return;

                b.withBounds(a.getBounds()).withType(AnnotationTypes.ANNOTATION_TYPE_PERSON).save();
              });
    }
  }
}
