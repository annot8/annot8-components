/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.Annot8RuntimeException;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static io.annot8.conventions.PropertyKeys.PROPERTY_KEY_LANGUAGE;

/**
 * Detect the language of a Text content and add the language as an annotation covering the whole
 * Content.
 */
@ComponentName("Detect Language")
@ComponentDescription("Detect the language of text content")
public class DetectLanguage extends AbstractProcessorDescriptor<DetectLanguage.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_LANGUAGE, ContentBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private final LanguageDetector languageDetector;

    public Processor(LanguageDetector languageDetector) {
      this.languageDetector = languageDetector;
    }

    public Processor(){
      List<LanguageProfile> languageProfiles;
      try{
        languageProfiles = new LanguageProfileReader().readAllBuiltIn();
      }catch (IOException ioe){
        throw new Annot8RuntimeException("Could not read built in language profiles");
      }

      this.languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
          .withProfiles(languageProfiles)
          .build();
    }

    @Override
    public void process(Text text) {
      TextObject textObject = CommonTextObjectFactories.forDetectingOnLargeText().forText(text.getData());
      Optional<LdLocale> lang = languageDetector.detect(textObject).toJavaUtil();

      if (lang.isPresent()) {
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_LANGUAGE)
            .withBounds(ContentBounds.getInstance())
            .withProperty(PROPERTY_KEY_LANGUAGE, lang.get().getLanguage())
            .save();
      }
    }
  }
}