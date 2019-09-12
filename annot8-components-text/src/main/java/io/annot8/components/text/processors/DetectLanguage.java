/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.text.TextObject;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.components.text.processors.settings.DetectLanguageSettings;
import io.annot8.conventions.AnnotationTypes;

import java.util.Optional;

import static io.annot8.conventions.PropertyKeys.PROPERTY_KEY_LANGUAGE;

/**
 * Detect the language of a Text content and add the language as an annotation covering the whole
 * Content.
 */
//@ProcessesContent(Text.class)
//@CreatesAnnotation(value = AnnotationTypes.ANNOTATION_TYPE_LANGUAGE, bounds = ContentBounds.class)
public class DetectLanguage extends AbstractTextProcessor {


    private final DetectLanguageSettings detectLanguageSettings;
    private final LanguageDetector languageDetector;

    public DetectLanguage(DetectLanguageSettings settings) {
      this.detectLanguageSettings = settings;

      languageDetector =
              LanguageDetectorBuilder.create(detectLanguageSettings.getNgramExtractor())
                      .withProfiles(detectLanguageSettings.getLanguageProfiles())
                      .build();
  }

  @Override
  public void process(Text text) {


          TextObject textObject =
              detectLanguageSettings.getTextObjectFactory().forText(text.getData());
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
