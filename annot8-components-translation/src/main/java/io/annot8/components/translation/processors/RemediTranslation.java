/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.translation.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.PropertyKeys;
import uk.gov.nca.remedi4j.client.RemediClient;

/**
 * Use the REMEDI machine translation platform (see
 * https://github.com/ivan-zapreev/Distributed-Translation-Infrastructure) to perform translation of
 * Text content objects.
 */
@ComponentName("REMEDI Translation")
@ComponentDescription("Uses the REMEDI Machine Translation tool to translate text between languages")
@SettingsClass(RemediTranslationSettings.class)
public class RemediTranslation extends AbstractProcessorDescriptor<RemediTranslation.Processor, RemediTranslationSettings> {

  @Override
  protected Processor createComponent(Context context, RemediTranslationSettings settings) {
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

    private RemediClient client = null;
    private String source = null;
    private String target = null;

    public Processor(RemediTranslationSettings settings) {
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
}
