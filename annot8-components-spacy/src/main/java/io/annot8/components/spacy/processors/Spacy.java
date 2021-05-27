/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.List;
import org.openapi.spacy.ApiException;
import org.openapi.spacy.model.PartsOfSpeech;
import org.openapi.spacy.model.PartsOfSpeechTags;

@ComponentName("SpaCy POS")
@ComponentDescription(
    "Use SpaCy (via SpaCy Server) to annotate Sentences, Tokens, Part of Speech tags and Entities in text")
@SettingsClass(Spacy.Settings.class)
@ComponentTags({"spacy", "sentences", "tokens", "pos", "ner"})
public class Spacy extends AbstractProcessorDescriptor<Spacy.Processor, Spacy.Settings> {

  @Override
  protected Processor createComponent(Context context, Spacy.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder().withProcessesContent(Text.class);

    if (getSettings().isAddSentences())
      builder.withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class);

    if (getSettings().isAddTokens())
      builder.withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class);

    if (getSettings().isAddEntities())
      SpacyNER.Processor.nerLabelMapping
          .values()
          .forEach(v -> builder.withCreatesAnnotations(v, SpanBounds.class));

    return builder.build();
  }

  public static class Processor extends SpacyServerProcessor {
    private final Spacy.Settings settings;

    public Processor(Spacy.Settings settings) {
      super(settings);

      this.settings = settings;
    }

    @Override
    protected void process(Text content) {
      PartsOfSpeech pos;
      try {
        pos = client.pos(fromTextContent(content));
      } catch (ApiException e) {
        throw new ProcessingException("An error occurred whilst using the SpaCy POS API", e);
      }

      pos.getData()
          .forEach(
              sentence -> {
                List<PartsOfSpeechTags> tags = sentence.getTags();

                if (tags.isEmpty()) return;

                int lastTagIndex = tags.size() - 1;
                int sentenceEnd =
                    tags.get(lastTagIndex).getCharOffset()
                        + tags.get(lastTagIndex).getText().length();

                if (settings.isAddSentences()) {
                  int sentenceBegin = tags.get(0).getCharOffset();

                  // Create sentence
                  content
                      .getAnnotations()
                      .create()
                      .withBounds(new SpanBounds(sentenceBegin, sentenceEnd))
                      .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
                      .save();
                }

                // If we're not adding tokens or entities, then speed things up by skipping to the
                // next sentence
                if (!(settings.isAddTokens() || settings.isAddEntities())) return;

                int entityBegin = -1;
                int entityEnd = -1;
                String entityType = null;

                for (int i = 0; i <= lastTagIndex; i++) {
                  PartsOfSpeechTags tag = tags.get(i);

                  if (settings.isAddTokens()) {
                    // Create token
                    content
                        .getAnnotations()
                        .create()
                        .withBounds(
                            new SpanBounds(
                                tag.getCharOffset(), tag.getCharOffset() + tag.getText().length()))
                        .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
                        .withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, tag.getTag())
                        .withProperty(PropertyKeys.PROPERTY_KEY_LEMMA, tag.getLemma())
                        .withProperty(PropertyKeys.PROPERTY_KEY_LANGUAGE, tag.getLang())
                        .save();
                  }

                  if (settings.isAddEntities()) {
                    // Create entities
                    if (tag.getEntIob().equals(PartsOfSpeechTags.EntIobEnum.B)) {
                      entityBegin = tag.getCharOffset();
                      entityEnd = tag.getCharOffset() + tag.getText().length();
                      entityType = tag.getEntType();
                    } else if (tag.getEntIob().equals(PartsOfSpeechTags.EntIobEnum.I)) {
                      entityEnd = tag.getCharOffset() + tag.getText().length();
                    } else if (entityBegin != -1) {
                      content
                          .getAnnotations()
                          .create()
                          .withBounds(new SpanBounds(entityBegin, entityEnd))
                          .withType(toNerLabel(entityType))
                          .save();

                      entityBegin = -1;
                      entityEnd = -1;
                    }
                  }
                }

                // Handle case where entity is at very end of sentence
                if (settings.isAddEntities() && entityBegin != -1) {
                  content
                      .getAnnotations()
                      .create()
                      .withBounds(new SpanBounds(entityBegin, sentenceEnd))
                      .withType(toNerLabel(entityType))
                      .save();
                }
              });
    }
  }

  public static class Settings extends SpacyServerSettings {
    boolean addSentences = true;
    boolean addTokens = true;
    boolean addEntities = true;

    @Override
    public boolean validate() {
      return super.validate() && (addSentences || addTokens || addEntities);
    }

    @Description("If false, then sentence annotations won't be added by this processor")
    public boolean isAddSentences() {
      return addSentences;
    }

    public void setAddSentences(boolean addSentences) {
      this.addSentences = addSentences;
    }

    @Description("If false, then word token annotations won't be added by this processor")
    public boolean isAddTokens() {
      return addTokens;
    }

    public void setAddTokens(boolean addTokens) {
      this.addTokens = addTokens;
    }

    @Description("If false, then entity annotations won't be added by this processor")
    public boolean isAddEntities() {
      return addEntities;
    }

    public void setAddEntities(boolean addEntities) {
      this.addEntities = addEntities;
    }
  }
}
