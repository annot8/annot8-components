/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static io.annot8.conventions.PropertyKeys.PROPERTY_KEY_LANGUAGE;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;

/**
 * Detect the language of a Text content and add the language as an annotation covering the whole
 * Content.
 */
@ComponentName("Detect Language")
@ComponentDescription("Detect the language of text content")
public class DetectLanguage
    extends AbstractProcessorDescriptor<DetectLanguage.Processor, NoSettings> {

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

    public Processor(Language... languages) {
      this.languageDetector = LanguageDetectorBuilder.fromLanguages(languages).build();
    }

    public Processor() {
      this.languageDetector = LanguageDetectorBuilder.fromAllBuiltInLanguages().build();
    }

    @Override
    public void process(Text text) {
      Language lang = languageDetector.detectLanguageOf(text.getData());

      if (lang == Language.UNKNOWN) return;

      text.getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_LANGUAGE)
          .withBounds(ContentBounds.getInstance())
          .withProperty(PROPERTY_KEY_LANGUAGE, lang.getIsoCode639_1().toString())
          .save();
    }
  }
}
