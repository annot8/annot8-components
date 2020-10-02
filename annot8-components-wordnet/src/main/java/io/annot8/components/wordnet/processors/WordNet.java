/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.wordnet.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.Annot8RuntimeException;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.Optional;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.Dictionary;

@ComponentName("WordNet Lemmas")
@ComponentDescription("Add lemmas to word tokens using WordNet")
public class WordNet extends AbstractProcessorDescriptor<WordNet.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings noSettings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private Dictionary dictionary;

    public Processor() {
      try {
        // With Java 9 modules this will throw IllegalArgumentException.
        // Which means that the class.getResourceAsStream('path/in/jar') returns null;
        // We assume this is something to do with accessing the "/extjwnl_resource_properties.xml"
        // resource.
        // If we change the code below to get the inputstream itself you can some success, but the
        // code fails later.
        dictionary = Dictionary.getDefaultResourceInstance();
      } catch (JWNLException | IllegalArgumentException e) {
        // IllegalArgumentException is thrown if inputstream is null, but JWNLException.
        throw new Annot8RuntimeException("Could not load WordNet dictionary", e);
      }
    }

    @Override
    protected void process(Text content) {
      content
          .getAnnotations()
          .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
          .filter(a -> a.getProperties().has(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, String.class))
          .forEach(
              a -> {
                POS pos =
                    toPos(
                        a.getProperties()
                            .get(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, String.class)
                            .get());
                String word = content.getText(a).get();

                if (pos != null) {
                  Optional<IndexWord> lookupWord = lookupWord(pos, word);

                  if (lookupWord.isPresent()) {
                    content
                        .getAnnotations()
                        .copy(a)
                        .withProperty(PropertyKeys.PROPERTY_KEY_LEMMA, lookupWord.get().getLemma())
                        .save();

                    content.getAnnotations().delete(a);
                  }
                }
              });
    }

    @Override
    public void close() {
      try {
        dictionary.close();
      } catch (final JWNLException e) {
        log().warn("WordNet dictionary did not close cleanly", e);
      } finally {
        dictionary = null;
      }
    }

    /**
     * Convert a string (Penntree bank / simple word) to a Part of speech type.
     *
     * @param pos the pos
     * @return the pos
     */
    public static POS toPos(String pos) {
      final String lc = pos.toUpperCase();

      POS ret = null;

      if (lc.startsWith("N")) {
        ret = POS.NOUN;
      } else if (lc.startsWith("V")) {
        ret = POS.VERB;
      } else if (lc.startsWith("R") || lc.startsWith("ADV")) {
        ret = POS.ADVERB;
      } else if (lc.startsWith("J") || lc.startsWith("ADJ")) {
        ret = POS.ADJECTIVE;
      }

      return ret;
    }

    /**
     * Lookup the word from the dictionary, performing lemmisation if required.
     *
     * @param pos the pos
     * @param word the word
     * @return the WordNet word, (as an optional)
     */
    public Optional<IndexWord> lookupWord(final POS pos, final String word) {
      try {
        return Optional.ofNullable(dictionary.lookupIndexWord(pos, word));
      } catch (final JWNLException e) {
        log().warn("Lookup word {} failed", word, e);
        return Optional.empty();
      }
    }
  }
}
