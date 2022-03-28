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
import io.annot8.conventions.PropertyKeys;
import java.util.List;
import org.openapi.spacy.ApiException;
import org.openapi.spacy.model.NERRequest;
import org.openapi.spacy.model.NERResponse;

@ComponentName("SpaCy Named Entity Recognition")
@ComponentDescription("Use SpaCy NER model (via SpaCy Server) to extract entities")
@SettingsClass(SpacyServerSettings.class)
@ComponentTags({"spacy", "ner"})
public class SpacyNER extends AbstractProcessorDescriptor<SpacyNER.Processor, SpacyServerSettings> {

  @Override
  protected Processor createComponent(Context context, SpacyServerSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder().withProcessesContent(Text.class);

    Processor.nerLabelMapping
        .values()
        .forEach(v -> builder.withCreatesAnnotations(v, SpanBounds.class));

    return builder.build();
  }

  // Deep type hierarchy
  @SuppressWarnings("java:S110")
  public static class Processor extends SpacyServerProcessor {
    public Processor(SpacyServerSettings settings) {
      super(settings);
    }

    @Override
    protected void process(Text content) {
      NERRequest request = new NERRequest();
      request.setSections(List.of(content.getData()));
      request.setSense2vec(false);

      NERResponse response;
      try {
        response = client.ner(request);
      } catch (ApiException e) {
        throw new ProcessingException("An error occurred whilst using the SpaCy NER API", e);
      }

      response
          .getData()
          .forEach(
              d ->
                  d.getEntities()
                      .forEach(
                          e -> {
                            content
                                .getAnnotations()
                                .create()
                                .withBounds(new SpanBounds(e.getStartChar(), e.getEndChar()))
                                .withType(toNerLabel(e.getLabel()))
                                .withProperty(PropertyKeys.PROPERTY_KEY_LEMMA, e.getLemma())
                                .save();
                          }));
    }
  }
}
