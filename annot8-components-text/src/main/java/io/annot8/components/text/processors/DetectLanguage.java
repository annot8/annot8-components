/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static io.annot8.conventions.PropertyKeys.PROPERTY_KEY_LANGUAGE;

import java.util.Optional;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.text.TextObject;

import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.components.text.processors.settings.DetectLanguageSettings;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.core.capabilities.CreatesAnnotation;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;

/**
 * Detect the language of a Text content and add the language as an annotation covering the whole
 * Content.
 */
@ProcessesContent(Text.class)
@CreatesAnnotation(value = AnnotationTypes.ANNOTATION_TYPE_LANGUAGE, bounds = ContentBounds.class)
public class DetectLanguage extends AbstractComponent implements Processor {

  private LanguageDetector languageDetector;
  private DetectLanguageSettings detectLanguageSettings;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    // Get settings
    Optional<DetectLanguageSettings> opt = context.getSettings(DetectLanguageSettings.class);
    detectLanguageSettings = opt.orElse(new DetectLanguageSettings());

    languageDetector =
        LanguageDetectorBuilder.create(detectLanguageSettings.getNgramExtractor())
            .withProfiles(detectLanguageSettings.getLanguageProfiles())
            .build();
  }

  @Override
  public ProcessorResponse process(Item item) {

    item.getContents(Text.class)
        .filter(t -> t.getData() != null)
        .forEach(
            t -> {
              TextObject textObject =
                  detectLanguageSettings.getTextObjectFactory().forText(t.getData());
              Optional<LdLocale> lang = languageDetector.detect(textObject).toJavaUtil();

              if (lang.isPresent()) {
                t.getAnnotations()
                    .create()
                    .withType(AnnotationTypes.ANNOTATION_TYPE_LANGUAGE)
                    .withBounds(ContentBounds.getInstance())
                    .withProperty(PROPERTY_KEY_LANGUAGE, lang.get().getLanguage())
                    .save();
              }
            });

    return ProcessorResponse.ok();
  }
}
