/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.capabilities.AnnotationCapability;
import io.annot8.core.settings.NoSettings;
import io.annot8.core.stores.AnnotationStore;
import org.iban4j.Iban4jException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class IBAN extends AbstractTextProcessor<NoSettings> {

  private static final Pattern IBAN_PATTERN =
      Pattern.compile(
          "\\b([A-Z]{2})\\s*([0-9]{2})\\s*(([A-Z0-9]{4}\\s*){2,7}[A-Z0-9]{1,4})\\b",
          Pattern.CASE_INSENSITIVE);

  @Override
  protected void process(Text content) {
    Matcher m = IBAN_PATTERN.matcher(content.getData());
    AnnotationStore annotationStore = content.getAnnotations();

    while (m.find()) {
      String code = m.group().replaceAll("\\s*", "").toUpperCase();

      try {
        org.iban4j.Iban iban = org.iban4j.Iban.valueOf(code);

        annotationStore
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_FINANCIALACCOUNT)
            .withBounds(new SpanBounds(m.start(), m.end()))
            .withProperty(PropertyKeys.PROPERTY_KEY_ACCOUNTNUMBER, iban.getAccountNumber())
            .withProperty(PropertyKeys.PROPERTY_KEY_BANKCODE, iban.getBankCode())
            .withProperty(PropertyKeys.PROPERTY_KEY_BRANCHCODE, iban.getBranchCode())
            .withProperty(PropertyKeys.PROPERTY_KEY_COUNTRY, iban.getCountryCode().getAlpha2())
            .save();

      } catch (Iban4jException e) {
        // Not a valid IBAN, so continue
      }
    }
  }

  @Override
  public Stream<AnnotationCapability> createsAnnotations() {
    return Stream.of(new AnnotationCapability(AnnotationTypes.ANNOTATION_TYPE_FINANCIALACCOUNT, SpanBounds.class));
  }
}
