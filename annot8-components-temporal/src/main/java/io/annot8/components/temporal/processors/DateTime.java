/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.temporal.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.components.temporal.processors.utils.DateTimeUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Annotate date time strings as Temporal entities. The following examples show the types of date
 * times that are detected.
 *
 * <ul>
 *   <li>ISO8601 Format
 *   <li>0725hrs on 9 Sept 15
 *   <li>22 Apr 2014 1529 UTC
 * </ul>
 */
@ComponentName("Date Time") // The display name of the processor
@ComponentDescription("Extracts formatted dates and times from text")
public class DateTime extends AbstractProcessorDescriptor<DateTime.Processor, NoSettings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  public static class Processor extends AbstractTextProcessor {

    private static final String DAYS =
        "(?:(?:Mon|Monday|Tue|Tues|Tuesday|Wed|Wednesday|Thu|Thurs|Thursday|Fri|Friday|Sat|Saturday|Sun|Sunday)\\s+)?"; // Non-capturing as we don't use this information
    private static final String MONTHS =
        "(Jan(uary)?|Feb(ruary)?|Mar(ch)?|Apr(il)?|May|Jun(e)?|Jul(y)?|Aug(ust)?|Sep(t)?(ember)?|Oct(ober)?|Nov(ember)?|Dec(ember)?)";
    private static final String DATE_SUFFIXES = "(st|nd|rd|th)";
    private static final String TIME_ZONES =
        Arrays.stream(TimeZone.getAvailableIDs())
            .filter(s -> s.length() <= 3)
            .filter(s -> s.equals(s.toUpperCase()))
            .collect(Collectors.joining("|"));

    @Override
    protected void process(Text content) {
      processIso(content);
      processTimeOnDate(content);
      processDayMonthTime(content);
      processMonthDayTime(content);
    }

    private void processIso(Text content) {
      Pattern iso8601 =
          Pattern.compile(
              "\\b(\\d{4})-?(\\d{2})-?(\\d{2})[T ](\\d{2}):?(\\d{2}):?(\\d{2})(\\.\\d{3})?\\s?(Z|[-+]\\d{2}:\\d{2})?\\b");
      Matcher m = iso8601.matcher(content.getData());

      while (m.find()) {
        try {
          Temporal t;
          if (m.group(8) == null) {
            // No time zone, so assume LocalDateTime
            t =
                LocalDateTime.parse(
                    m.group().replaceAll(" ", "T"), DateTimeFormatter.ISO_DATE_TIME);
          } else {
            // Time zone information, so use ZonedDateTime
            t =
                ZonedDateTime.parse(
                    m.group().replaceAll(" ", "T"), DateTimeFormatter.ISO_DATE_TIME);
          }

          createDateTime(content, m.start(), m.end(), t);
        } catch (DateTimeParseException dtpe) {
          log().debug("Unable to parse date time " + m.group() + dtpe);
        }
      }
    }

    private void processTimeOnDate(Text content) {
      Pattern timeOnDate =
          Pattern.compile(
              "\\b([01][0-9]|2[0-3]):?([0-5][0-9]):?([0-5][0-9])?(hrs)? on ([0-2]?[0-9]|3[01]) "
                  + MONTHS
                  + " (\\d{4}|'?\\d{2})\\b",
              Pattern.CASE_INSENSITIVE);
      Matcher m = timeOnDate.matcher(content.getData());

      while (m.find()) {
        LocalDateTime t;
        if (m.group(3) != null) {
          t =
              LocalDateTime.of(
                  DateTimeUtils.asYear(m.group(19)).getValue(),
                  DateTimeUtils.asMonth(m.group(6)).getValue(),
                  Integer.parseInt(m.group(5)),
                  Integer.parseInt(m.group(1)),
                  Integer.parseInt(m.group(2)),
                  Integer.parseInt(m.group(3)));
        } else {
          t =
              LocalDateTime.of(
                  DateTimeUtils.asYear(m.group(19)).getValue(),
                  DateTimeUtils.asMonth(m.group(6)).getValue(),
                  Integer.parseInt(m.group(5)),
                  Integer.parseInt(m.group(1)),
                  Integer.parseInt(m.group(2)));
        }

        createDateTime(content, m.start(), m.end(), t);
      }
    }

    private void processDayMonthTime(Text content) {
      Pattern dayMonthTime =
          Pattern.compile(
              "\\b"
                  + DAYS
                  + "([0-2]?[0-9]|3[01])\\s*"
                  + DATE_SUFFIXES
                  + "?\\s+"
                  + MONTHS
                  + ",?\\s+(\\d{4}|'?\\d{2})\\s+([01][0-9]|2[0-3]):?([0-5][0-9]):?([0-5][0-9])?\\s*(Z|"
                  + TIME_ZONES
                  + ")?\\b",
              Pattern.CASE_INSENSITIVE);
      Matcher m = dayMonthTime.matcher(content.getData());

      while (m.find()) {
        LocalDateTime ldt;
        if (m.group(19) != null) {
          ldt =
              LocalDateTime.of(
                  DateTimeUtils.asYear(m.group(16)).getValue(),
                  DateTimeUtils.asMonth(m.group(3)).getValue(),
                  Integer.parseInt(m.group(1)),
                  Integer.parseInt(m.group(17)),
                  Integer.parseInt(m.group(18)),
                  Integer.parseInt(m.group(19)));
        } else {
          ldt =
              LocalDateTime.of(
                  DateTimeUtils.asYear(m.group(16)).getValue(),
                  DateTimeUtils.asMonth(m.group(3)).getValue(),
                  Integer.parseInt(m.group(1)),
                  Integer.parseInt(m.group(17)),
                  Integer.parseInt(m.group(18)));
        }

        ZoneId zone;
        if (m.group(20) == null) {
          createDateTime(content, m.start(), m.end(), ldt);
        } else {
          zone = TimeZone.getTimeZone(m.group(20)).toZoneId();
          createDateTime(content, m.start(), m.end(), ZonedDateTime.of(ldt, zone));
        }
      }
    }

    private void processMonthDayTime(Text content) {
      Pattern monthDayTime =
          Pattern.compile(
              "\\b"
                  + MONTHS
                  + "\\s+([0-2]?[0-9]|3[01])\\s*"
                  + DATE_SUFFIXES
                  + "?,?\\s+(\\d{4}|'?\\d{2})\\s+([01][0-9]|2[0-3]):?([0-5][0-9]):?([0-5][0-9])?\\s*(Z|"
                  + TIME_ZONES
                  + ")?\\b",
              Pattern.CASE_INSENSITIVE);
      Matcher m = monthDayTime.matcher(content.getData());

      while (m.find()) {
        LocalDateTime ldt;
        if (m.group(19) != null) {
          ldt =
              LocalDateTime.of(
                  DateTimeUtils.asYear(m.group(16)).getValue(),
                  DateTimeUtils.asMonth(m.group(1)).getValue(),
                  Integer.parseInt(m.group(14)),
                  Integer.parseInt(m.group(17)),
                  Integer.parseInt(m.group(18)),
                  Integer.parseInt(m.group(19)));
        } else {
          ldt =
              LocalDateTime.of(
                  DateTimeUtils.asYear(m.group(16)).getValue(),
                  DateTimeUtils.asMonth(m.group(1)).getValue(),
                  Integer.parseInt(m.group(14)),
                  Integer.parseInt(m.group(17)),
                  Integer.parseInt(m.group(18)));
        }

        ZoneId zone;
        if (m.group(20) == null) {
          createDateTime(content, m.start(), m.end(), ldt);
        } else {
          zone = TimeZone.getTimeZone(m.group(20)).toZoneId();
          createDateTime(content, m.start(), m.end(), ZonedDateTime.of(ldt, zone));
        }
      }
    }

    private void createDateTime(Text content, int charBegin, int charEnd, Temporal t) {

      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(charBegin, charEnd))
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT)
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, t)
          .save();
    }
  }
}
