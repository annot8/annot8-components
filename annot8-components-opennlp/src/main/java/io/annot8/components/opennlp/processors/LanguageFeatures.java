/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupTypes;

@ComponentName("OpenNLP Language Features")
@ComponentDescription(
    "Uses the Sentences, Tokens, POS and PhraseChunks processors to add language features to Text")
public class LanguageFeatures
    extends AbstractProcessorDescriptor<LanguageFeatures.Processor, NoSettings> {
  @Override
  protected Processor createComponent(Context context, NoSettings noSettings) {
    return new Processor(context);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withCreatesGroups(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private Sentences.Processor sentences;
    private Tokens.Processor tokens;
    private POS.Processor pos;
    private PhraseChunks.Processor chunks;

    public Processor(Context context) {
      Sentences s = new Sentences();
      sentences = s.createComponent(context, new Sentences.Settings());

      Tokens t = new Tokens();
      tokens = t.createComponent(context, new Tokens.Settings());

      POS p = new POS();
      pos = p.createComponent(context, new POS.Settings());

      PhraseChunks c = new PhraseChunks();
      chunks = c.createComponent(context, new PhraseChunks.Settings());
    }

    @Override
    protected void process(Text content) {
      sentences.process(content);
      tokens.process(content);
      pos.process(content);
      chunks.process(content);
    }

    @Override
    public void close() {
      sentences.close();
      tokens.close();
      pos.close();
      chunks.close();
    }
  }
}
