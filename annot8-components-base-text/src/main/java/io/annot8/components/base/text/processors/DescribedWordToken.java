/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.text.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.components.stopwords.resources.NoOpStopwords;
import io.annot8.components.stopwords.resources.Stopwords;
import io.annot8.conventions.AnnotationTypes;
import java.util.Map;
import java.util.Set;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

@ComponentName("Described Word Token")
@ComponentDescription(
    "Finds word tokens matching a pre-defined list, and then looks for additional descriptors preceding these")
@SettingsClass(DescribedWordToken.Settings.class)
public class DescribedWordToken
    extends AbstractProcessorDescriptor<DescribedWordToken.Processor, DescribedWordToken.Settings> {
  @Override
  protected Processor createComponent(Context context, Settings settings) {
    Stopwords stopwords = context.getResource(Stopwords.class).orElse(new NoOpStopwords());
    return new Processor(
        stopwords,
        settings.getType(),
        settings.getRootTokens(),
        settings.getDescriptors(),
        settings.isRequireDescriptors(),
        settings.getProperties());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withCreatesAnnotations(getSettings().getType(), SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private final String type;
    private final Set<String> rootTokens;
    private final Set<String> descriptors;
    private final boolean requireDescriptors;
    private final Map<String, Object> properties;

    private final Stopwords stopwords;

    public Processor(
        Stopwords stopwords,
        String type,
        Set<String> rootTokens,
        Set<String> descriptors,
        boolean requireDescriptors,
        Map<String, Object> properties) {
      this.stopwords = stopwords;
      this.type = type;
      this.rootTokens = rootTokens;
      this.descriptors = descriptors;
      this.requireDescriptors = requireDescriptors;
      this.properties = properties;
    }

    @Override
    protected void process(Text content) {
      content
          .getAnnotations()
          .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .forEach(
              sentence -> {
                SpanBounds sentenceSpan = sentence.getBounds(SpanBounds.class).get();
                content
                    .getBetween(sentenceSpan.getBegin(), sentenceSpan.getEnd())
                    .filter(a -> AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN.equals(a.getType()))
                    .filter(
                        a ->
                            rootTokens.stream()
                                .anyMatch(s -> s.equalsIgnoreCase(content.getText(a).orElse(""))))
                    .forEach(a -> findDescriptorsAndCreate(content, sentenceSpan, a));
              });
    }

    protected void findDescriptorsAndCreate(
        Text content, SpanBounds sentence, Annotation rootWord) {
      SpanBounds rootSpan = rootWord.getBounds(SpanBounds.class).get();

      int begin =
          content
              .getBetween(sentence.getBegin(), rootSpan.getBegin())
              .filter(a -> AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN.equals(a.getType()))
              .sorted(SortUtils.SORT_BY_SPANBOUNDS.reversed())
              .takeWhile(
                  a -> {
                    String w = content.getText(a).orElse("");
                    return stopwords.isStopword(w)
                        || descriptors.stream().anyMatch(s -> s.equalsIgnoreCase(w));
                  })
              .sorted(SortUtils.SORT_BY_SPANBOUNDS)
              .dropWhile(a -> stopwords.isStopword(content.getText(a).orElse("")))
              .mapToInt(a -> a.getBounds(SpanBounds.class).get().getBegin())
              .min()
              .orElse(rootSpan.getBegin());

      if (requireDescriptors && rootSpan.getBegin() == begin) return;

      Annotation.Builder builder =
          content
              .getAnnotations()
              .create()
              .withType(type)
              .withBounds(new SpanBounds(begin, rootSpan.getEnd()));

      for (Map.Entry<String, Object> e : properties.entrySet()) {
        builder = builder.withProperty(e.getKey(), e.getValue());
      }

      builder.save();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private final String type;
    private final Set<String> rootTokens;
    private final Set<String> descriptors;
    private final boolean requireDescriptors;
    private final Map<String, Object> properties;

    @JsonbCreator
    public Settings(
        @JsonbProperty("type") String type,
        @JsonbProperty("rootTokens") Set<String> rootTokens,
        @JsonbProperty("descriptors") Set<String> descriptors,
        @JsonbProperty("requireDescriptors") boolean requireDescriptors,
        @JsonbProperty("properties") Map<String, Object> properties) {
      this.type = type;
      this.rootTokens = rootTokens;
      this.descriptors = descriptors;
      this.requireDescriptors = requireDescriptors;
      this.properties = properties;
    }

    @Override
    public boolean validate() {
      return type != null
          && !type.isEmpty()
          && rootTokens != null
          && !rootTokens.isEmpty()
          && descriptors != null
          && properties != null;
    }

    @Description("The type to assign to annotations")
    public String getType() {
      return type;
    }

    @Description("The set of root tokens to look for")
    public Set<String> getRootTokens() {
      return rootTokens;
    }

    @Description("The set of allowed descriptors")
    public Set<String> getDescriptors() {
      return descriptors;
    }

    @Description("Is at least one descriptor required?")
    public boolean isRequireDescriptors() {
      return requireDescriptors;
    }

    @Description("Additional properties to add to matches")
    public Map<String, Object> getProperties() {
      return properties;
    }
  }
}
