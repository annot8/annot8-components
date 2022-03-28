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
import org.openapi.spacy.model.Tokens;

@ComponentName("SpaCy Sentences")
@ComponentDescription("Use SpaCy (via SpaCy Server) to annotate tokens in text")
@SettingsClass(SpacyServerSettings.class)
@ComponentTags({"spacy", "tokens"})
public class SpacyTokens
    extends AbstractProcessorDescriptor<SpacyTokens.Processor, SpacyServerSettings> {

  @Override
  protected Processor createComponent(Context context, SpacyServerSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .build();
  }

  // Deep type hierarchy
  @SuppressWarnings("java:S110")
  public static class Processor extends SpacyServerProcessor {

    public Processor(SpacyServerSettings settings) {
      super(settings);
    }

    @Override
    protected void process(Text content) {
      Tokens tokens;
      try {
        tokens = client.tokenizer(fromTextContent(content));
      } catch (ApiException e) {
        throw new ProcessingException("An error occurred whilst using the SpaCy Tokens API", e);
      }

      int pos = 0;

      List<String> tokLoop = new ArrayList<>(tokens.getTokens());
      while (!tokLoop.isEmpty()) {
        String tok = tokLoop.remove(0);

        while (pos < content.getData().length()) {
          if (content.getData().startsWith(tok, pos)) {
            // Create sentence
            content
                .getAnnotations()
                .create()
                .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
                .withBounds(new SpanBounds(pos, pos + tok.length()))
                .save();

            pos += tok.length();
            break;
          } else {
            pos++;
          }
        }
      }
    }
  }
}
