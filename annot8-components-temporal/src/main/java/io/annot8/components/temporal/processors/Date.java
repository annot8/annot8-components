/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.temporal.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.components.temporal.processors.utils.DateTimeUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Annotate dates and date ranges as Temporal/Instant entities. The following examples show the
 * types of dates and ranges that are detected.
 *
 * <ul>
 *   <li>1 December 2016
 *   <li>December 1 2016
 *   <li>2016-12-01
 *   <li>1/12/2016
 *   <li>2011-14
 *   <li>2011-2016
 *   <li>March 2015
 *   <li>late August 2016
 *   <li>June-September 2015
 *   <li>June 2015 - September 2016
 *   <li>10-15 Jan 2015
 *   <li>10/11 Jan 2015
 *   <li>27th September - 4th October 2016
 *   <li>23 December 2016 - 2nd January 2017
 * </ul>
 *
 * The word 'to' is supported in place of a hyphen, as is the word 'and' if the expression is
 * preceded by 'between'.
 *
 * <p>Years on their own will only extracted for the range 1970-2099 to reduce false positives. Two
 * digit years on their own will not be extracted.
 */
@ComponentName("Date") // The display name of the processor
@ComponentDescription("Extracts formatted dates from text")
@SettingsClass(Date.Settings.class)
public class Date extends AbstractProcessorDescriptor<Date.Processor, Date.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INTERVAL, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getAmericanDates());
  }

  public static class Processor extends AbstractTextProcessor {

    private final boolean americanDates;

    // Non-capturing as we don't use this information
    private static final String DAYS =
        "(?:(?:Mon|Monday|Tue|Tues|Tuesday|Wed|Wednesday|Thu|Thurs|Thursday|Fri|Friday|Sat|Saturday|Sun|Sunday)\\s+)?";
    private static final String MONTHS =
        "(Jan(\\.|uary)?|Feb(\\.|ruary)?|Mar(\\.|ch)?|Apr(\\.|il)?|May|Jun([.e])?|Jul([.y])?|Aug(\\.|ust)?|Sep(\\.|t(\\.|ember)?)?|Oct(\\.|ober)?|Nov(\\.|ember)?|Dec(\\.|ember)?)";
    private static final String DATES = "([1-9]|[12][0-9]|3[01])\\s*";
    private static final String DATE_SUFFIXES = "(st|nd|rd|th)";

    private static final String INVALID_DATE_FOUND = "Invalid date found";

    public Processor(boolean americanDates) {
      this.americanDates = americanDates;
    }

    @Override
    protected void process(Text content) {
      List<SpanBounds> extracted = new ArrayList<>();

      // Order here is important, as we want to identify the ranges first
      // so that we can ignore things that have already been extracted
      identifyYearRanges(content, extracted);
      identifyMonthYearRanges(content, extracted);
      identifyDayMonthYearRanges(content, extracted);
      identifyDates(content, extracted);
      identifyMonths(content, extracted);
      identifyYears(content, extracted);
    }

    private void identifyYearRanges(Text content, List<SpanBounds> extracted) {
      String text = content.getData();

      // e.g. 2017-19
      Pattern longYearShortYear =
          Pattern.compile("\\b(\\d{2})(\\d{2})-(\\d{2})\\b", Pattern.CASE_INSENSITIVE);
      Matcher m = longYearShortYear.matcher(text);

      while (m.find()) {
        if (dateSeparatorSuffix(text, m.end())) {
          continue;
        }

        Year y1 = Year.parse(m.group(1) + m.group(2));
        Year y2 = Year.parse(m.group(1) + m.group(3));

        createYearTimeRange(content, m.start(), m.end(), y1, y2, extracted);
      }

      // e.g. 2017-2019, 2017 to 2019, between 2017 and 2019
      Pattern longYearLongYear =
          Pattern.compile("\\b(\\d{4})\\s*(-|to|and)\\s*(\\d{4})\\b", Pattern.CASE_INSENSITIVE);
      m = longYearLongYear.matcher(text);

      while (m.find()) {
        if ("and".equalsIgnoreCase(m.group(2)) && !betweenPrefix(text, m.start())) {
          continue;
        }

        Year y1 = Year.parse(m.group(1));
        Year y2 = Year.parse(m.group(3));

        createYearTimeRange(content, m.start(), m.end(), y1, y2, extracted);
      }
    }

    private void createYearTimeRange(
        Text content,
        int charBegin,
        int charEnd,
        Year y1,
        Year y2,
        Collection<SpanBounds> extracted) {
      if (alreadyExtracted(extracted, charBegin, charEnd)) return;

      SpanBounds sb = new SpanBounds(charBegin, charEnd);

      content
          .getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INTERVAL)
          .withBounds(sb)
          .withProperty(PropertyKeys.PROPERTY_KEY_START, y1)
          .withProperty(PropertyKeys.PROPERTY_KEY_END, y2)
          .save();
      extracted.add(sb);
    }

    private void identifyMonthYearRanges(Text content, List<SpanBounds> extracted) {
      String text = content.getData();

      // e.g. Mar-Apr 1997, between March and April 97, March to Apr '97
      Pattern sameYear =
          Pattern.compile(
              "\\b" + MONTHS + "\\s*(-|to|and)\\s*" + MONTHS + "\\s+(\\d{4}|'?\\d{2})\\b",
              Pattern.CASE_INSENSITIVE);
      Matcher m = sameYear.matcher(text);

      while (m.find()) {
        if ("and".equalsIgnoreCase(m.group(14)) && !betweenPrefix(text, m.start())) {
          continue;
        }

        Year y = DateTimeUtils.asYear(m.group(28));

        String m1 = m.group(1);
        if (m1.endsWith(".")) {
          m1 = m1.substring(0, m1.length() - 1);
        }

        String m2 = m.group(15);
        if (m2.endsWith(".")) {
          m2 = m2.substring(0, m2.length() - 1);
        }

        YearMonth ym1 = y.atMonth(DateTimeUtils.asMonth(m1));
        YearMonth ym2 = y.atMonth(DateTimeUtils.asMonth(m2));

        createMonthYearTimeRange(content, m.start(), m.end(), ym1, ym2, extracted);
      }

      // e.g. March 97 to June 98, between Mar '97 and Jun 1998
      Pattern diffYear =
          Pattern.compile(
              "\\b"
                  + MONTHS
                  + "\\s+(\\d{4}|'?\\d{2})\\s*(-|to|and)\\s*"
                  + MONTHS
                  + "\\s+(\\d{4}|'?\\d{2})\\b",
              Pattern.CASE_INSENSITIVE);
      m = diffYear.matcher(text);

      while (m.find()) {
        if ("and".equalsIgnoreCase(m.group(15)) && !betweenPrefix(text, m.start())) {
          continue;
        }

        String m1 = m.group(1);
        if (m1.endsWith(".")) {
          m1 = m1.substring(0, m1.length() - 1);
        }

        String m2 = m.group(16);
        if (m2.endsWith(".")) {
          m2 = m2.substring(0, m2.length() - 1);
        }

        Year y1 = DateTimeUtils.asYear(m.group(14));
        YearMonth ym1 = y1.atMonth(DateTimeUtils.asMonth(m1));

        Year y2 = DateTimeUtils.asYear(m.group(29));
        YearMonth ym2 = y2.atMonth(DateTimeUtils.asMonth(m2));

        createMonthYearTimeRange(content, m.start(), m.end(), ym1, ym2, extracted);
      }
    }

    private void createMonthYearTimeRange(
        Text content,
        int charBegin,
        int charEnd,
        YearMonth ym1,
        YearMonth ym2,
        Collection<SpanBounds> extracted) {
      if (alreadyExtracted(extracted, charBegin, charEnd)) return;

      SpanBounds sb = new SpanBounds(charBegin, charEnd);

      content
          .getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INTERVAL)
          .withBounds(sb)
          .withProperty(PropertyKeys.PROPERTY_KEY_START, ym1)
          .withProperty(PropertyKeys.PROPERTY_KEY_END, ym2)
          .save();

      extracted.add(sb);
    }

    private void identifyDayMonthYearRanges(Text content, List<SpanBounds> extracted) {
      // e.g. Monday 23rd to Thursday 26 Sept '19, 4-12 June 2020
      Pattern sameMonth =
          Pattern.compile(
              "\\b"
                  + DAYS
                  + "([0-2]?[0-9]|3[01])\\s*"
                  + DATE_SUFFIXES
                  + "?\\s*(-|to|and|\\\\|/)\\s*"
                  + DAYS
                  + "([0-2]?[0-9]|3[01])\\s*"
                  + DATE_SUFFIXES
                  + "?\\s+"
                  + MONTHS
                  + "\\s+(\\d{4}|'?\\d{2})\\b",
              Pattern.CASE_INSENSITIVE);
      String text = content.getData();
      Matcher m = sameMonth.matcher(text);

      while (m.find()) {
        if (!DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(1)), m.group(2))
            || !DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(4)), m.group(5))) {
          continue;
        }

        Year y = DateTimeUtils.asYear(m.group(19));

        String month = m.group(6);
        if (month.endsWith(".")) {
          month = month.substring(0, month.length() - 1);
        }

        YearMonth ym = y.atMonth(DateTimeUtils.asMonth(month));

        LocalDate ld1;
        LocalDate ld2;
        try {
          ld1 = ym.atDay(Integer.parseInt(m.group(1)));
          ld2 = ym.atDay(Integer.parseInt(m.group(4)));
        } catch (DateTimeException dte) {
          log().warn(INVALID_DATE_FOUND + dte);
          continue;
        }

        if ("and".equalsIgnoreCase(m.group(3)) && !betweenPrefix(text, m.start())
            || "/".equals(m.group(3))
            || "\\".equals(m.group(3))) {
          if (ld2.equals(ld1.plusDays(1))) {
            // Create time range
            createDayMonthYearRange(content, m.start(), m.end(), ld1, ld2, extracted);
          } else {
            // Create separate dates as they're not adjacent
            createDate(content, m.start(4), m.end(), ld2, extracted);
            createDate(content, m.start(), m.end(), ld1, extracted);
          }
        } else {
          // Create time range
          createDayMonthYearRange(content, m.start(), m.end(), ld1, ld2, extracted);
        }
      }

      // e.g. Monday 26th Aug to Friday 27th September 2019
      Pattern sameYear =
          Pattern.compile(
              "\\b"
                  + DAYS
                  + DATES
                  + DATE_SUFFIXES
                  + "?\\s+"
                  + MONTHS
                  + "\\s*(-|to|and)\\s*"
                  + DAYS
                  + DATES
                  + DATE_SUFFIXES
                  + "?\\s+"
                  + MONTHS
                  + "\\s+(\\d{4}|'?\\d{2})\\b",
              Pattern.CASE_INSENSITIVE);
      m = sameYear.matcher(text);

      while (m.find()) {
        boolean suffixesCorrect =
            DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(1)), m.group(2))
                && DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(17)), m.group(18));
        boolean andNotBetween =
            "and".equalsIgnoreCase(m.group(16)) && !betweenPrefix(text, m.start());

        if (!suffixesCorrect || andNotBetween) {
          continue;
        }

        String m1 = m.group(3);
        if (m1.endsWith(".")) {
          m1 = m1.substring(0, m1.length() - 1);
        }

        String m2 = m.group(19);
        if (m2.endsWith(".")) {
          m2 = m2.substring(0, m2.length() - 1);
        }

        Year y = DateTimeUtils.asYear(m.group(32));
        YearMonth ym1 = y.atMonth(DateTimeUtils.asMonth(m1));
        YearMonth ym2 = y.atMonth(DateTimeUtils.asMonth(m2));

        try {
          createDayMonthYearRange(
              content,
              m.start(),
              m.end(),
              ym1.atDay(Integer.parseInt(m.group(1))),
              ym2.atDay(Integer.parseInt(m.group(17))),
              extracted);
        } catch (DateTimeException dte) {
          log().warn(INVALID_DATE_FOUND + dte);
        }
      }

      // Between 2 January 2018 and 1 January 2019
      Pattern fullDates =
          Pattern.compile(
              "\\b"
                  + DAYS
                  + "([0-2]?[0-9]|3[01])\\s*"
                  + DATE_SUFFIXES
                  + "?\\s+"
                  + MONTHS
                  + "\\s+(\\d{4}|'?\\d{2})\\s*(-|to|and)\\s*"
                  + DAYS
                  + "([0-2]?[0-9]|3[01])\\s*"
                  + DATE_SUFFIXES
                  + "?\\s+"
                  + MONTHS
                  + "\\s+(\\d{4}|'?\\d{2})\\b",
              Pattern.CASE_INSENSITIVE);
      m = fullDates.matcher(text);

      while (m.find()) {
        boolean suffixesCorrect =
            DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(1)), m.group(2))
                && DateTimeUtils.suffixCorrect(Integer.parseInt(m.group(18)), m.group(19));
        boolean andNotBetween =
            "and".equalsIgnoreCase(m.group(17)) && !betweenPrefix(text, m.start());

        if (!suffixesCorrect || andNotBetween) {
          continue;
        }

        String m1 = m.group(3);
        if (m1.endsWith(".")) {
          m1 = m1.substring(0, m1.length() - 1);
        }

        String m2 = m.group(20);
        if (m2.endsWith(".")) {
          m2 = m2.substring(0, m2.length() - 1);
        }

        Year y1 = DateTimeUtils.asYear(m.group(16));
        YearMonth ym1 = y1.atMonth(DateTimeUtils.asMonth(m1));

        Year y2 = DateTimeUtils.asYear(m.group(33));
        YearMonth ym2 = y2.atMonth(DateTimeUtils.asMonth(m2));

        try {
          createDayMonthYearRange(
              content,
              m.start(),
              m.end(),
              ym1.atDay(Integer.parseInt(m.group(1))),
              ym2.atDay(Integer.parseInt(m.group(18))),
              extracted);
        } catch (DateTimeException dte) {
          log().warn(INVALID_DATE_FOUND + dte);
        }
      }
    }

    private void createDayMonthYearRange(
        Text content,
        int charBegin,
        int charEnd,
        LocalDate ld1,
        LocalDate ld2,
        Collection<SpanBounds> extracted) {
      if (alreadyExtracted(extracted, charBegin, charEnd)) return;

      SpanBounds sb = new SpanBounds(charBegin, charEnd);

      content
          .getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INTERVAL)
          .withBounds(sb)
          .withProperty(PropertyKeys.PROPERTY_KEY_START, ld1)
          .withProperty(PropertyKeys.PROPERTY_KEY_END, ld2)
          .save();

      extracted.add(sb);
    }

    private void identifyDates(Text content, List<SpanBounds> extracted) {
      // e.g. 4 November 1998, 8th May '01
      Pattern fullDateDayMonth =
          Pattern.compile(
              "\\b"
                  + DAYS
                  + DATES
                  + DATE_SUFFIXES
                  + "?\\s+"
                  + MONTHS
                  + ",?\\s+(\\d{4}|'?\\d{2}\\b)",
              Pattern.CASE_INSENSITIVE);
      String text = content.getData();
      Matcher m = fullDateDayMonth.matcher(text);

      while (m.find()) {
        createDateFromMatcher(content, m, 16, 3, 1, extracted);
      }

      // e.g. November 4 1998, May 8th '01
      Pattern fullDateMonthDay =
          Pattern.compile(
              "\\b"
                  + MONTHS
                  + "\\s+([0-2]?[0-9]|3[01])\\s*"
                  + DATE_SUFFIXES
                  + "?,?\\s+(\\d{4}|'?\\d{2}\\b)",
              Pattern.CASE_INSENSITIVE);
      m = fullDateMonthDay.matcher(text);

      while (m.find()) {
        createDateFromMatcher(content, m, 16, 1, 14, extracted);
      }

      Pattern shortDateYearFirst =
          Pattern.compile(
              "\\b(\\d{4})[-\\\\/.](0?[1-9]|1[0-2])[-\\\\/.]([0-2]?[0-9]|3[01])\\b",
              Pattern.CASE_INSENSITIVE);
      m = shortDateYearFirst.matcher(text);

      while (m.find()) {
        createDateFromMatcher(content, m, 1, 2, 3, extracted);
      }

      // e.g. 4/11/98, 08-05-01
      Pattern shortDate =
          Pattern.compile(
              "\\b([0-2]?[0-9]|3[01])[-\\\\/.]([0-2]?[0-9]|3[01])[-\\\\/.](\\d{4}|\\d{2})\\b",
              Pattern.CASE_INSENSITIVE);
      m = shortDate.matcher(text);

      while (m.find()) {
        Year y = DateTimeUtils.asYear(m.group(3));

        int n1 = Integer.parseInt(m.group(1));
        int n2 = Integer.parseInt(m.group(2));

        int day;
        int month;
        if (n1 >= 1 && n1 <= 12) {
          // n1 could be a month or a day
          if (n2 > 12 && n2 <= 31) {
            // n2 must be a day
            month = n1;
            day = n2;
          } else if (n2 >= 1 && n2 <= 12) {
            if (americanDates) {
              day = n2;
              month = n1;
            } else {
              day = n1;
              month = n2;
            }
          } else {
            // invalid combination of n1 and n2
            continue;
          }
        } else if (n1 > 12 && n1 <= 31) {
          // n1 must be a day
          day = n1;
          if (n2 >= 1 && n2 <= 12) {
            // n2 must be a month
            month = n2;
          } else {
            // invalid combination of n1 and n2
            continue;
          }
        } else {
          // n1 can't be a month or a day
          continue;
        }

        YearMonth ym = y.atMonth(month);

        LocalDate ld;
        try {
          ld = ym.atDay(day);
        } catch (DateTimeException dte) {
          log().warn(INVALID_DATE_FOUND + dte);
          continue;
        }

        createDate(content, m.start(), m.end(), ld, extracted);
      }
    }

    private void createDate(
        Text content, int charBegin, int charEnd, LocalDate ld, Collection<SpanBounds> extracted) {
      if (alreadyExtracted(extracted, charBegin, charEnd)) return;

      SpanBounds sb = new SpanBounds(charBegin, charEnd);

      content
          .getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT)
          .withBounds(sb)
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, ld)
          .save();

      extracted.add(sb);
    }

    private static boolean alreadyExtracted(
        Collection<SpanBounds> extracted, Integer begin, Integer end) {
      return extracted.stream().anyMatch(sb -> sb.getBegin() <= begin && sb.getEnd() >= end);
    }

    private void identifyMonths(Text content, List<SpanBounds> extracted) {
      // e.g. Beginning of May 2012, Mid-April 1997, Late Jun '12
      Pattern monthYear =
          Pattern.compile(
              "\\b((beginning of|start of|early|mid|late|end of)[- ])?"
                  + MONTHS
                  + "\\s+(\\d{4}|'?\\d{2}\\b)",
              Pattern.CASE_INSENSITIVE);
      String text = content.getData();
      Matcher m = monthYear.matcher(text);

      while (m.find()) {
        Year y = DateTimeUtils.asYear(m.group(16));
        String month = m.group(3);

        if (month.endsWith(".")) {
          month = month.substring(0, month.length() - 1);
        }

        YearMonth ym = y.atMonth(DateTimeUtils.asMonth(month));

        if (m.group(2) != null) {
          LocalDate ld1;
          LocalDate ld2;
          switch (m.group(2).toLowerCase()) {
            case "beginning of":
            case "start of":
              ld1 = ym.atDay(1);
              ld2 = ym.atDay(5);
              break;
            case "early":
              ld1 = ym.atDay(1);
              ld2 = ym.atDay(10);
              break;
            case "mid":
              ld1 = ym.atDay(11);
              ld2 = ym.atDay(20);
              break;
            case "late":
              ld1 = ym.atDay(21);
              ld2 = ym.atEndOfMonth();
              break;
            case "end of":
              ld1 = ym.atEndOfMonth().minusDays(5);
              ld2 = ym.atEndOfMonth();
              break;
            default:
              continue;
          }

          createDayMonthYearRange(content, m.start(), m.end(), ld1, ld2, extracted);
        } else {
          createMonth(content, m.start(), m.end(), ym, extracted);
        }
      }
    }

    private void createMonth(
        Text content, int charBegin, int charEnd, YearMonth ym, Collection<SpanBounds> extracted) {
      if (alreadyExtracted(extracted, charBegin, charEnd)) return;

      SpanBounds sb = new SpanBounds(charBegin, charEnd);

      content
          .getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT)
          .withBounds(sb)
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, ym)
          .save();

      extracted.add(sb);
    }

    private void identifyYears(Text content, List<SpanBounds> extracted) {
      // e.g. 1997, 2012
      Pattern monthYear =
          Pattern.compile("\\b(19[789][0-9]|20[0-9][0-9])\\b", Pattern.CASE_INSENSITIVE);
      String text = content.getData();
      Matcher m = monthYear.matcher(text);

      while (m.find()) {
        Year y = DateTimeUtils.asYear(m.group(1));

        createYear(content, m.start(), m.end(), y, extracted);
      }
    }

    private void createYear(
        Text content, int charBegin, int charEnd, Year y, Collection<SpanBounds> extracted) {
      if (alreadyExtracted(extracted, charBegin, charEnd)) return;

      SpanBounds sb = new SpanBounds(charBegin, charEnd);

      content
          .getAnnotations()
          .create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT)
          .withBounds(sb)
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, y)
          .save();

      extracted.add(sb);
    }

    private static boolean betweenPrefix(String text, Integer matchStart) {
      return text.substring(0, matchStart).trim().toLowerCase().endsWith("between");
    }

    private static boolean dateSeparatorSuffix(String text, Integer matchEnd) {
      if (matchEnd >= text.length()) {
        return false;
      }

      String nextChar = text.substring(matchEnd, matchEnd + 1);
      return "-".equals(nextChar) || "/".equals(nextChar) || "\\".equals(nextChar);
    }

    private void createDateFromMatcher(
        Text content,
        Matcher m,
        int yearGroup,
        int monthGroup,
        int dayGroup,
        Collection<SpanBounds> extracted) {
      Year y = DateTimeUtils.asYear(m.group(yearGroup));

      String month = m.group(monthGroup);
      if (month.endsWith(".")) {
        month = month.substring(0, month.length() - 1);
      }

      YearMonth ym = y.atMonth(DateTimeUtils.asMonth(month));

      LocalDate ld;
      try {
        ld = ym.atDay(Integer.parseInt(m.group(dayGroup)));
      } catch (DateTimeException dte) {
        log().warn(INVALID_DATE_FOUND + dte);
        return;
      }

      createDate(content, m.start(), m.end(), ld, extracted);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private boolean americanDates = true;

    @Description("Should we use American dates where applicable (i.e. mm-dd-yy)?")
    public boolean getAmericanDates() {
      return americanDates;
    }

    public void setAmericanDates(boolean americaDates) {
      this.americanDates = americaDates;
    }

    @Override
    public boolean validate() {
      // invalid settings are not possible
      return true;
    }
  }
}
