/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import java.io.IOException;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;

/** Tokenizes words and sentences using OpenNLP tokenization models */
@ComponentDescription("Tokenizes words and sentences using OpenNLP tokenization models")
public class Tokenizer extends AbstractTextProcessor {

  private SentenceDetectorME sentenceDetector;
  private TokenizerME wordTokenizer;

  public Tokenizer() {
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

  //  @Override
  //  public Stream<AnnotationCapability> createsAnnotations() {
  //    return StreamUtils.append(
  //        super.createsAnnotations(),
  //        new AnnotationCapability(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class),
  //        new AnnotationCapability(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class));
  //  }
}
