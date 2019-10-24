/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import java.io.IOException;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

/** Tokenizes words and sentences using OpenNLP tokenization models */
@ComponentName("OpenNLP Tokenizer")
@ComponentDescription("Tokenizes words and sentences using OpenNLP tokenization models")
public class Tokenizer extends AbstractProcessorDescriptor<Tokenizer.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private SentenceDetectorME sentenceDetector;
    private TokenizerME wordTokenizer;

    public Processor() {
      // TODO: Allow users to provide their own models
      SentenceModel sentenceModel;
      try {
        sentenceModel = new SentenceModel(getClass().getResourceAsStream("en-sent.bin"));
      } catch (IOException e) {
        throw new BadConfigurationException("Unable to load sentence model");
      }

      TokenizerModel wordTokenModel;
      try {
        wordTokenModel = new TokenizerModel(getClass().getResourceAsStream("en-token.bin"));
      } catch (IOException e) {
        throw new BadConfigurationException("Unable to load word tokenizer model");
      }

      sentenceDetector = new SentenceDetectorME(sentenceModel);
      wordTokenizer = new TokenizerME(wordTokenModel);
    }

    @Override
    protected void process(Text content) {
      String textContent = content.getData();

      for (Span sentence : sentenceDetector.sentPosDetect(textContent)) {
        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
            .withBounds(new SpanBounds(sentence.getStart(), sentence.getEnd()))
            .save();

        for (Span token :
            wordTokenizer.tokenizePos(
                textContent.substring(sentence.getStart(), sentence.getEnd()))) {
          content
              .getAnnotations()
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
              .withBounds(
                  new SpanBounds(
                      sentence.getStart() + token.getStart(), sentence.getStart() + token.getEnd()))
              .save();
        }
      }
    }

    @Override
    public void close() {
      sentenceDetector = null;
      wordTokenizer = null;
    }
  }
}
