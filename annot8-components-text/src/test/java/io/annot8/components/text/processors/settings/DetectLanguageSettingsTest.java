/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.BuiltInLanguages;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileBuilder;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObjectFactory;

public class DetectLanguageSettingsTest {
  @Test
  public void testLanguageProfiles() {
    DetectLanguageSettings dls = new DetectLanguageSettings();

    assertNotNull(dls.getLanguageProfiles());
    assertEquals(BuiltInLanguages.getLanguages().size(), dls.getLanguageProfiles().size());

    LanguageProfile lp = new LanguageProfileBuilder(LdLocale.fromString("en")).build();

    dls.setLanguageProfiles(Arrays.asList(lp));

    assertNotNull(dls.getLanguageProfiles());
    assertEquals(1, dls.getLanguageProfiles().size());
  }

  @Test
  public void testNgramExtractor() {
    DetectLanguageSettings dls = new DetectLanguageSettings();

    assertEquals(NgramExtractors.standard(), dls.getNgramExtractor());

    dls.setNgramExtractor(NgramExtractors.backwards());
    assertEquals(NgramExtractors.backwards(), dls.getNgramExtractor());
  }

  @Test
  public void testTextObjectFactory() {
    DetectLanguageSettings dls = new DetectLanguageSettings();

    TextObjectFactory tof = CommonTextObjectFactories.forDetectingShortCleanText();

    dls.setTextObjectFactory(tof);
    assertEquals(tof, dls.getTextObjectFactory());
  }
}
