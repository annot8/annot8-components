/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.financial.processors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class SortCode extends AbstractTextProcessor {

  private static final Pattern SORT_CODE_PATTERN =
      Pattern.compile("\\b([0-9]{2})-([0-9]{2})-([0-9]{2})\\b");

  @Override
  protected void process(Item item, Text content) throws Annot8Exception {
    Matcher m = SORT_CODE_PATTERN.matcher(content.getData());
    AnnotationStore annotationStore = content.getAnnotations();

    while (m.find()) {

      annotationStore
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_FINANCIALACCOUNT)
          .withBounds(new SpanBounds(m.start(), m.end()))
          .withProperty(PropertyKeys.PROPERTY_KEY_BRANCHCODE, m.group().replaceAll("-", ""))
          .save();
    }
  }
}
