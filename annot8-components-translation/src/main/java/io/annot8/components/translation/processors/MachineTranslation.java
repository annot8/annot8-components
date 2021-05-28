/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.translation.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.PropertyKeys;
import java.util.Collection;
import uk.gov.dstl.machinetranslation.connector.api.LanguagePair;
import uk.gov.dstl.machinetranslation.connector.api.MTConnectorApi;
import uk.gov.dstl.machinetranslation.connector.api.Translation;
import uk.gov.dstl.machinetranslation.connector.api.exceptions.ConfigurationException;
import uk.gov.dstl.machinetranslation.connector.api.exceptions.ConnectorException;
import uk.gov.dstl.machinetranslation.connector.api.utils.ConnectorUtils;

/**
 * Uses the MT API (see https://github.com/dstl/machinetranslation) to perform translation of Text
 * content objects. The relevant connector must be on the class path
 */
@ComponentName("Machine Translation")
@ComponentDescription("Uses the Machine Translation API to translate text between languages")
@SettingsClass(MachineTranslationSettings.class)
@ComponentTags({"translation", "text"})
public class MachineTranslation
    extends AbstractProcessorDescriptor<MachineTranslation.Processor, MachineTranslationSettings> {
  @Override
  protected Processor createComponent(Context context, MachineTranslationSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private final MTConnectorApi connector;
    private final String sourceLanguage;
    private final String targetLanguage;

    private final boolean copyProperties;

    protected Processor(
        String sourceLanguage,
        String targetLanguage,
        boolean copyProperties,
        MTConnectorApi connector) {
      this.sourceLanguage = sourceLanguage;
      this.targetLanguage = targetLanguage;
      this.copyProperties = copyProperties;
      this.connector = connector;
    }

    public Processor(MachineTranslationSettings settings) {
      this.sourceLanguage = settings.getSourceLanguage();
      this.targetLanguage = settings.getTargetLanguage();

      this.copyProperties = settings.isCopyProperties();

      try {
        connector = settings.getTranslatorClass().getConstructor().newInstance();
      } catch (Exception e) {
        throw new BadConfigurationException("Could not instantiate MT Connector", e);
      }

      try {
        connector.configure(settings.getTranslatorConfiguration());
      } catch (ConfigurationException e) {
        throw new BadConfigurationException("Could not configure MT Connector", e);
      }

      if (!ConnectorUtils.LANGUAGE_AUTO.equals(sourceLanguage)
          && connector.queryEngine().isSupportedLanguagesSupported()) {
        try {
          Collection<LanguagePair> supportedLanguages = connector.supportedLanguages();

          if (!supportedLanguages.contains(new LanguagePair(sourceLanguage, targetLanguage))) {
            throw new BadConfigurationException(
                "Unsupported language pair (" + sourceLanguage + " -> " + targetLanguage + ")");
          }
        } catch (ConnectorException e) {
          log().error("Unable to retrieve supported languages", e);
        }
      }
    }

    @Override
    protected void process(Text content) {
      Translation translatedText;
      try {
        log()
            .debug("Translating {} from {} to {}", content.getId(), sourceLanguage, targetLanguage);
        translatedText = connector.translate(sourceLanguage, targetLanguage, content.getData());
      } catch (ConnectorException e) {
        throw new ProcessingException("Unable to translate text", e);
      }

      Content.Builder<Text, String> builder =
          content
              .getItem()
              .createContent(Text.class)
              .withDescription(
                  "Translated "
                      + content.getId()
                      + " from "
                      + translatedText.getSourceLanguage()
                      + " to "
                      + targetLanguage
                      + " by "
                      + connector.queryEngine().getName())
              .withData(translatedText.getContent());

      if (copyProperties) builder = builder.withProperties(content.getProperties());

      builder.withProperty(PropertyKeys.PROPERTY_KEY_LANGUAGE, targetLanguage).save();
    }
  }
}
