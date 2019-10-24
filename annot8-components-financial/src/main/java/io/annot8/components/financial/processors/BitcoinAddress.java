/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;

@ComponentName("Bitcoin Address")
@ComponentDescription("Extract valid Bitcoin addresses from text")
public class BitcoinAddress
    extends AbstractProcessorDescriptor<BitcoinAddress.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_FINANCIALACCOUNT, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    public static final Pattern BITCOIN_PATTERN =
        Pattern.compile("\\b([13][a-zA-Z0-9]{25,34})|(bc1)[a-zA-Z0-9]{23,32}\\b");

    @Override
    protected void process(Text content) {
      Matcher m = BITCOIN_PATTERN.matcher(content.getData());
      AnnotationStore annotationStore = content.getAnnotations();

      while (m.find()) {
        String value = m.group();
        // Validate address to reduce false positives
        try {
          Base58.decodeChecked(value);
        } catch (AddressFormatException afe) {
          continue;
        }

        String accountType;
        if (value.startsWith("1")) {
          accountType = "bitcoin#P2PKH";
        } else if (value.startsWith("3")) {
          accountType = "bitcoin#P2SH";
        } else if (value.startsWith("bc1")) {
          accountType = "bitcoin#Bech32";
        } else {
          // Shouldn't be possible to get here, but default to this just in case...
          accountType = "bitcoin";
        }

        annotationStore
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_FINANCIALACCOUNT)
            .withBounds(new SpanBounds(m.start(), m.end()))
            .withProperty(PropertyKeys.PROPERTY_KEY_ACCOUNTTYPE, accountType)
            .save();
      }
    }
  }
}
