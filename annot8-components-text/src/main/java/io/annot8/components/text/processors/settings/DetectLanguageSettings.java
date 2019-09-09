/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors.settings;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.optimaize.langdetect.ngram.NgramExtractor;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObjectFactory;

import io.annot8.core.settings.Settings;

public class DetectLanguageSettings implements Settings {

  private Optional<List<LanguageProfile>> languageProfiles = Optional.empty();
  private Optional<NgramExtractor> ngramExtractor = Optional.empty();
  private Optional<TextObjectFactory> textObjectFactory = Optional.empty();

  @Override
  public boolean validate() {
    return true;
  }

  public List<LanguageProfile> getLanguageProfiles() {
    if (languageProfiles.isPresent()) {
      return languageProfiles.get();
    } else {
      try {
        return new LanguageProfileReader().readAllBuiltIn();
      } catch (IOException ioe) {
        // TODO: Log an error here
        return Collections.emptyList();
      }
    }
  }

  public void setLanguageProfiles(List<LanguageProfile> languageProfiles) {
    this.languageProfiles = Optional.of(languageProfiles);
  }

  public NgramExtractor getNgramExtractor() {
    return ngramExtractor.orElse(NgramExtractors.standard());
  }

  public void setNgramExtractor(NgramExtractor ngramExtractor) {
    this.ngramExtractor = Optional.of(ngramExtractor);
  }

  public TextObjectFactory getTextObjectFactory() {
    return textObjectFactory.orElse(CommonTextObjectFactories.forDetectingOnLargeText());
  }

  public void setTextObjectFactory(TextObjectFactory textObjectFactory) {
    this.textObjectFactory = Optional.of(textObjectFactory);
  }
}
