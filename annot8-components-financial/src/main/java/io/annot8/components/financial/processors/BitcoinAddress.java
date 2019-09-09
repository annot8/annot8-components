/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.settings.EmptySettings;
import io.annot8.core.settings.SettingsClass;
import io.annot8.core.stores.AnnotationStore;

@SettingsClass(EmptySettings.class)
public class BitcoinAddress extends AbstractTextProcessor {

  public static final Pattern BITCOIN_PATTERN = Pattern.compile("\\b[13][a-zA-Z0-9]{25,34}\\b");

  @Override
  protected void process(Item item, Text content) throws Annot8Exception {
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
