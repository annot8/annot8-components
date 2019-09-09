/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.translation.processors;

import java.util.Optional;

import uk.gov.nca.remedi4j.client.RemediClient;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.exceptions.ProcessingException;
import io.annot8.core.settings.SettingsClass;

/**
 * Use the REMEDI machine translation platform (see
 * https://github.com/ivan-zapreev/Distributed-Translation-Infrastructure) to perform translation of
 * Text content objects.
 */
@CreatesContent(Text.class)
@SettingsClass(RemediTranslationSettings.class)
public class RemediTranslation extends AbstractTextProcessor {

  private RemediClient client = null;
  private String source = null;
  private String target = null;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    Optional<RemediTranslationSettings> opt = context.getSettings(RemediTranslationSettings.class);
    if (!opt.isPresent()) {
      throw new BadConfigurationException("Must provide a RemediTranslationSettings object");
    }

    source = opt.get().getSourceLanguage();
    target = opt.get().getTargetLanguage();

    if (client != null) client.close();

    client =
        new RemediClient(
            opt.get().getPreProcessorUri(),
            opt.get().getServerUri(),
            opt.get().getPostProcessorUri());
  }

  @Override
  protected void process(Item item, Text content) throws Annot8Exception {
    try {
      String trans = client.translateText(source, target, content.getData()).get();

      item.create(Text.class)
          .withName(content.getName() + "_" + target)
          .withData(trans)
          .withProperty(PropertyKeys.PROPERTY_KEY_LANGUAGE, target)
          .save();
    } catch (Exception e) {
      throw new ProcessingException("Unable to translate text", e);
    }
  }

  @Override
  public void close() {
    if (client != null) client.close();
  }
}
