/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import java.util.ArrayList;
import java.util.List;
import org.openapi.spacy.ApiException;
import org.openapi.spacy.model.Sentences;

@ComponentName("SpaCy Sentences")
@ComponentDescription("Use SpaCy (via SpaCy Server) to annotate sentences in text")
@SettingsClass(SpacyServerSettings.class)
@ComponentTags({"spacy", "sentences"})
public class SpacySentences
    extends AbstractProcessorDescriptor<SpacySentences.Processor, SpacyServerSettings> {

  @Override
  protected Processor createComponent(Context context, SpacyServerSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .build();
  }

  public static class Processor extends SpacyServerProcessor {

    public Processor(SpacyServerSettings settings) {
      super(settings);
    }

    @Override
    protected void process(Text content) {
      Sentences sentences;
      try {
        sentences = client.sentencizer(fromTextContent(content));
      } catch (ApiException e) {
        throw new ProcessingException("An error occurred whilst using the SpaCy Sentences API", e);
      }

      int pos = 0;

      List<String> sentLoop = new ArrayList<>(sentences.getSentences());
      while (!sentLoop.isEmpty()) {
        String sent = sentLoop.remove(0);

        while (pos < content.getData().length()) {
          if (content.getData().startsWith(sent, pos)) {
            // Create sentence
            content
                .getAnnotations()
                .create()
                .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
                .withBounds(new SpanBounds(pos, pos + sent.length()))
                .save();

            pos += sent.length();
            break;
          } else {
            pos++;
          }
        }
      }
    }
  }
}
