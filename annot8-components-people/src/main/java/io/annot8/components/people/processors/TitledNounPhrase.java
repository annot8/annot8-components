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
import io.annot8.common.data.utils.SortUtils;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ComponentName("Titled Noun Phrase")
@ComponentDescription(
    "Identifies noun phrases that begin with a title (e.g. Miss) and annotates these as a person")
public class TitledNounPhrase
    extends AbstractProcessorDescriptor<TitledNounPhrase.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withProcessesGroups(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PERSON, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private static final String PROPERTY_KEY_RELIGION = "religion";

    private static final Map<String, Map<String, Object>> titles = new HashMap<>();

    static {
      // Standard titles
      titles.put("mr", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("mister", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("mrs", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("miss", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("ms", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("mx", Map.of());
      titles.put("master", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("madam", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));

      // Academic titles
      titles.put("dr", Map.of());
      titles.put("doctor", Map.of());
      titles.put("prof", Map.of());
      titles.put("professor", Map.of());

      // Family titles
      titles.put("aunt", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("auntie", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("uncle", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));

      // Aristocratic/ruling titles
      titles.put("sir", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("dame", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("lord", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("lady", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("king", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("his royal highness", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("his majesty", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("queen", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("her royal highness", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("her majesty", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("hrh", Map.of());
      titles.put("emperor", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("empress", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("viceroy", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("vicereine", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("grand duke", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("grand duchess", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("archduke", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("archduchess", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("prince", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("princess", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("duke", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("duchess", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("earl", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("count", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("countess", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("viscount", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("viscountess", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("baron", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("baroness", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("chief", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("chieftess", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("sultan", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("sultana", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("tsar", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("tsarina", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("tsaritsa", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));
      titles.put("shah", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("marquess", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("marquis", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male"));
      titles.put("marchioness", Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "female"));

      // Religious titles
      titles.put("buddha", Map.of(PROPERTY_KEY_RELIGION, "buddhism"));
      titles.put("lama", Map.of(PROPERTY_KEY_RELIGION, "buddhism"));

      titles.put(
          "pope",
          Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "abbess",
          Map.of(
              PropertyKeys.PROPERTY_KEY_GENDER, "female", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "abbott",
          Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "brother",
          Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "sister",
          Map.of(
              PropertyKeys.PROPERTY_KEY_GENDER, "female", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "friar",
          Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "fr",
          Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "father",
          Map.of(PropertyKeys.PROPERTY_KEY_GENDER, "male", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "mother",
          Map.of(
              PropertyKeys.PROPERTY_KEY_GENDER, "female", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put(
          "mother superior",
          Map.of(
              PropertyKeys.PROPERTY_KEY_GENDER, "female", PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("rev", Map.of(PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("reverend", Map.of(PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("saint", Map.of(PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("bishop", Map.of(PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("archbishop", Map.of(PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("vicar", Map.of(PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("pastor", Map.of(PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("cannon", Map.of(PROPERTY_KEY_RELIGION, "christianity"));
      titles.put("cardinal", Map.of(PROPERTY_KEY_RELIGION, "christianity"));

      titles.put("caliph", Map.of(PROPERTY_KEY_RELIGION, "islam"));
      titles.put("imam", Map.of(PROPERTY_KEY_RELIGION, "islam"));
      titles.put("mullah", Map.of(PROPERTY_KEY_RELIGION, "islam"));
      titles.put("ayatollah", Map.of(PROPERTY_KEY_RELIGION, "islam"));

      titles.put("rabbi", Map.of(PROPERTY_KEY_RELIGION, "judaism"));
      titles.put("revve", Map.of(PROPERTY_KEY_RELIGION, "judaism"));

      titles.put("druid", Map.of(PROPERTY_KEY_RELIGION, "druidry"));

      // Political titles
      titles.put("pres", Map.of());
      titles.put("president", Map.of());
      titles.put("governor", Map.of());
      titles.put("senator", Map.of());
      titles.put("ambassador", Map.of());
      titles.put("mayor", Map.of());
      titles.put("envoy", Map.of());
      titles.put("prime minster", Map.of());
      titles.put("minister", Map.of());
      titles.put("councillor", Map.of());
      titles.put("representative", Map.of());
      titles.put("speaker", Map.of());
      titles.put("mp", Map.of());
      titles.put("emir", Map.of());
      titles.put("wali", Map.of());

      // Job titles
      titles.put("officer", Map.of());
      titles.put("captain", Map.of());
      titles.put("nurse", Map.of());
      titles.put("agent", Map.of());

      // Police titles
      titles.put("pc", Map.of());
      titles.put("police constable", Map.of());
      titles.put("constable", Map.of());
      titles.put("sergeant", Map.of());
      titles.put("inspector", Map.of());
      titles.put("chief inspector", Map.of());
      titles.put("detective", Map.of());
      titles.put("detective constable", Map.of());
      titles.put("detective sergeant", Map.of());
      titles.put("detective inspector", Map.of());
      titles.put("detective chief inspector", Map.of());
      titles.put("chief constable", Map.of());

      // Other titles
      titles.put("sheikh", Map.of());
      titles.put("shaykh", Map.of());
      titles.put("elder", Map.of());

      // Military titles are not included as there is an existing processor for these
    }

    @Override
    protected void process(Text content) {
      content
          .getItem()
          .getGroups()
          .getByType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
          .forEach(
              g -> {
                List<Annotation> annotations =
                    g.getAnnotationsForContent(content)
                        .filter(a -> AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN.equals(a.getType()))
                        .sorted(SortUtils.SORT_BY_SPANBOUNDS)
                        .collect(Collectors.toList());

                for (int i = annotations.size(); i > 0; i--) {
                  String s = getWords(annotations, content, i);
                  String sLower = s.toLowerCase();

                  if (titles.containsKey(sLower)) {
                    Map<String, Object> props = titles.get(sLower);

                    Annotation.Builder b =
                        content
                            .getAnnotations()
                            .create()
                            .withType(AnnotationTypes.ANNOTATION_TYPE_PERSON)
                            .withBounds(getSpan(annotations))
                            .withProperty(PropertyKeys.PROPERTY_KEY_TITLE, s);

                    props.forEach(b::withProperty);

                    b.save();

                    break;
                  }
                }
              });
    }

    private static String getWords(List<Annotation> annotations, Text content, int count) {
      // Assumes annotations are already sorted, which is done above
      return annotations.stream()
          .map(
              a ->
                  a.getBounds(SpanBounds.class)
                      .orElse(new SpanBounds(0, 0))
                      .getData(content)
                      .orElse(null))
          .limit(count)
          .filter(Objects::nonNull)
          .map(s -> s.replaceAll("\\.", ""))
          .collect(Collectors.joining(" "));
    }

    private static SpanBounds getSpan(List<Annotation> annotations) {
      List<SpanBounds> sb =
          annotations.stream()
              .map(a -> a.getBounds(SpanBounds.class).orElse(null))
              .filter(Objects::nonNull)
              .collect(Collectors.toList());

      // Assumes annotations are already sorted, which is done above
      return new SpanBounds(sb.get(0).getBegin(), sb.get(sb.size() - 1).getEnd());
    }
  }
}
