/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.translation.processors;

import uk.gov.nca.remedi4j.client.RemediClient;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.exceptions.ProcessingException;

/**
 * Use the REMEDI machine translation platform (see
 * https://github.com/ivan-zapreev/Distributed-Translation-Infrastructure) to perform translation of
 * Text content objects.
 */
public class RemediTranslation extends AbstractTextProcessor {

  private RemediClient client = null;
  private String source = null;
  private String target = null;

  RemediTranslation(RemediTranslationSettings settings) {
    source = settings.getSourceLanguage();
    target = settings.getTargetLanguage();

    if (client != null) client.close();

    client =
        new RemediClient(
            settings.getPreProcessorUri(), settings.getServerUri(), settings.getPostProcessorUri());
  }

  @Override
  protected void process(Text content) {
    try {
      String trans = client.translateText(source, target, content.getData()).get();

      content
          .getItem()
          .createContent(Text.class)
          .withDescription(
              String.format(
                  "Translated content[%s] from %s into %s", content.getId(), source, target))
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
