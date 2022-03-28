/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.quantities.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotate times within a document using regular expressions
 *
 * <p>The document content is searched for things that might represent time periods using regular
 * expressions. Any extracted times are normalized to seconds where possible (e.g. not months,
 * because the length of a month can vary). Years are assumed not to be leap years.
 *
 * <p>Any hour quantities that could be times, e.g. 2200hrs, are ignored.
 */
@ComponentName("Time Quantity")
@ComponentDescription(
    "The document content is searched for things that might represent time periods using regular expressions.")
@ComponentTags({"quantity", "time", "temporal", "text"})
public class Time extends AbstractProcessorDescriptor<Time.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_QUANTITY, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  // Deep type hierarchy
  @SuppressWarnings({"java:S110", "java:S5852"})
  public static class Processor extends AbstractQuantityProcessor {

    public static final int YEAR_TO_SECOND = 31536000;
    public static final int MONTH_TO_SECOND = 2628000; // differs from Baleen 2 - this is
    // YEAR_TO_SECOND / 12
    public static final int WEEK_TO_SECOND = 604800;
    public static final int DAY_TO_SECOND = 86400;

    public static final int HOUR_TO_SECOND = 3600;
    public static final int MINUTE_TO_SECOND = 60;

    public static final String UNIT = "s";

    private final Pattern yearPattern =
        Pattern.compile("\\b([0-9]+([0-9,]+[0-9])?)[ ]?(year|yr)(s)?\\b", Pattern.CASE_INSENSITIVE);
    private final Pattern monthPattern =
        Pattern.compile("\\b([0-9]+([0-9,]+[0-9])?)[ ]?(month)(s)?\\b", Pattern.CASE_INSENSITIVE);
    private final Pattern weekPattern =
        Pattern.compile("\\b([0-9]+([0-9,]+[0-9])?)[ ]?(week|wk)(s)?\\b", Pattern.CASE_INSENSITIVE);
    private final Pattern dayPattern =
        Pattern.compile("\\b([0-9]+([0-9,]+[0-9])?)[ ]?(day)(s)?\\b", Pattern.CASE_INSENSITIVE);
    private final Pattern hourPattern =
        Pattern.compile("\\b([0-9]+([0-9,]+[0-9])?)[ ]?(hour|hr)(s)?\\b", Pattern.CASE_INSENSITIVE);
    private final Pattern minutePattern =
        Pattern.compile(
            "\\b([0-9]+([0-9,]+[0-9])?)[ ]?(minute|min)(s)?\\b", Pattern.CASE_INSENSITIVE);
    private final Pattern secondPattern =
        Pattern.compile(
            "\\b([0-9]+([0-9,]+[0-9])?)[ ]?(second|sec)(s)?\\b", Pattern.CASE_INSENSITIVE);

    public Processor() {
      super(AnnotationTypes.ANNOTATION_TYPE_QUANTITY, UNIT);

      add(yearPattern, YEAR_TO_SECOND);
      add(monthPattern, MONTH_TO_SECOND);
      add(weekPattern, WEEK_TO_SECOND);
      add(dayPattern, DAY_TO_SECOND);
      add(minutePattern, MINUTE_TO_SECOND);
      add(secondPattern, 1);
    }

    @Override
    protected void process(Text content) {
      // Handle hours ourselves separately...

      Matcher matcher = hourPattern.matcher(content.getData());
      while (matcher.find()) {

        String q = matcher.group(1);
        if (q.length() == 4
            && Integer.parseInt(q.substring(0, 2)) <= 23
            && Integer.parseInt(q.substring(2)) <= 59) {
          continue;
        }

        content
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_QUANTITY)
            .withBounds(new SpanBounds(matcher.start(), matcher.end()))
            .withProperty(
                PropertyKeys.PROPERTY_KEY_VALUE,
                Double.parseDouble(matcher.group(1).replaceAll("[^0-9\\.]", "")) * HOUR_TO_SECOND)
            .withProperty(PropertyKeys.PROPERTY_KEY_UNIT, UNIT)
            .save();
      }

      // ...and then hand off to the AbstractQuantityProcessor for everything else
      super.process(content);
    }
  }
}
