/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.temporal.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.components.temporal.processors.utils.DateTimeUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract expressions that refer to a relative date, e.g. yesterday. These can be resolved by
 * providing a metadata field to check for the date that expressions are relative to.
 *
 * <p>Supported expressions are of the form:
 *
 * <ul>
 *   <li>day before yesterday
 *   <li>yesterday
 *   <li>today
 *   <li>tomorrow
 *   <li>day after tomorrow
 *   <li>this week
 *   <li>this month
 *   <li>this year
 *   <li>next Wednesday
 *   <li>last Wednesday
 *   <li>last week
 *   <li>next week
 *   <li>in the last week
 *   <li>in the next week
 *   <li>Monday last week
 *   <li>Monday next week
 *   <li>last month
 *   <li>next month
 *   <li>in the last month
 *   <li>in the next month
 *   <li>last year
 *   <li>next year
 *   <li>October last year
 *   <li>October next year
 *   <li>in the last year
 *   <li>in the next year
 *   <li>in the last x days/weeks/months/years
 * </ul>
 */
@ComponentName("Relative Date")
@ComponentDescription(
    "Extracts relative dates from text and resolves them if a document date is available")
@SettingsClass(RelativeDate.Settings.class)
public class RelativeDate
    extends AbstractProcessorDescriptor<RelativeDate.Processor, RelativeDate.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INTERVAL, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(
        settings.dateFormatter(), settings.getDateProperties(), settings.isUseTodayAsRelative());
  }

  @SuppressWarnings("java:S5843")
  public static class Processor extends AbstractTextProcessor {

    private DateTimeFormatter dtf;
    private Collection<String> dateProperties;
    private boolean useTodayAsRelative;

    private static final String DAYS =
        "(Mon|Monday|Tue|Tues|Tuesday|Wed|Wednesday|Thu|Thurs|Thursday|Fri|Friday|Sat|Saturday|Sun|Sunday)";

    private static final String MONTHS =
        "(January|Jan|February|Feb|March|Mar|April|Apr|May|June|Jun|July|Jul|August|Aug|September|Sept|Sep|October|Oct|November|Nov|December|Dec)";

    public Processor(
        DateTimeFormatter dateTimeFormatter,
        Collection<String> dateProperties,
        boolean useTodayAsRelative) {
      this.dtf = dateTimeFormatter;
      this.dateProperties = dateProperties;
      this.useTodayAsRelative = useTodayAsRelative;
    }

    @Override
    protected void process(Text content) {
      LocalDate relativeTo = fetchRelativeDate(content.getItem());

      yesterday(content, relativeTo);
      today(content, relativeTo);
      tomorrow(content, relativeTo);
      thisX(content, relativeTo);
      nextLastDay(content, relativeTo);
      nextLastWeek(content, relativeTo);
      nextLastMonth(content, relativeTo);
      nextLastYear(content, relativeTo);
      inTheNextLastX(content, relativeTo);
    }

    protected LocalDate fetchRelativeDate(Item item) {

      for (String field : dateProperties) {
        Optional<Object> opt = item.getProperties().get(field);
        if (opt.isPresent()) {
          Object obj = opt.get();
          if (obj instanceof LocalDate) {
            return (LocalDate) obj;
          } else if (obj instanceof LocalDateTime) {
            return ((LocalDateTime) obj).toLocalDate();
          } else if (obj instanceof ZonedDateTime) {
            return ((ZonedDateTime) obj).toLocalDate();
          } else if (obj instanceof Instant) {
            return LocalDate.ofInstant((Instant) obj, ZoneOffset.UTC);
          } else if (obj instanceof String && dtf != null) {
            String str = (String) obj;
            try {
              return LocalDate.parse(str, dtf);
            } catch (DateTimeParseException dtpe) {
              dtpe.printStackTrace();
              log().warn("Property {} found, but content ({}) wasn't parseable", field, str);
            }
          } else {
            log().warn("Property {} found, but content ({}) wasn't readable", field, obj);
          }
        }
      }

      if (useTodayAsRelative) return LocalDate.now();

      return null;
    }

    private void yesterday(Text content, LocalDate relativeTo) {
      Pattern p = Pattern.compile("\\b(day before )?yesterday\\b", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        if (m.group(1) != null) {
          createRelativeDay(content, m.start(), m.end(), -2, relativeTo);
        } else {
          createRelativeDay(content, m.start(), m.end(), -1, relativeTo);
        }
      }
    }

    private void today(Text content, LocalDate relativeTo) {
      Pattern p = Pattern.compile("\\btoday\\b", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        createRelativeDay(content, m.start(), m.end(), 0, relativeTo);
      }
    }

    private void tomorrow(Text content, LocalDate relativeTo) {
      Pattern p = Pattern.compile("\\b(day after )?tomorrow\\b", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        if (m.group(1) != null) {
          createRelativeDay(content, m.start(), m.end(), 2, relativeTo);
        } else {
          createRelativeDay(content, m.start(), m.end(), 1, relativeTo);
        }
      }
    }

    private void thisX(Text content, LocalDate relativeTo) {
      Pattern p = Pattern.compile("\\bthis (week|month|year)\\b", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        if ("week".equalsIgnoreCase(m.group(1))) {
          createRelativeWeek(content, m.start(), m.end(), 0, relativeTo);
        } else if ("month".equalsIgnoreCase(m.group(1))) {
          createRelativeMonth(content, m.start(), m.end(), 0, relativeTo);
        } else if ("year".equalsIgnoreCase(m.group(1))) {
          createRelativeYear(content, m.start(), m.end(), 0, relativeTo);
        }
      }
    }

    private void nextLastDay(Text content, LocalDate relativeTo) {
      Pattern p = Pattern.compile("\\b(next|last) " + DAYS + "\\b", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        Integer offset = null;

        if (relativeTo != null) {
          if ("next".equalsIgnoreCase(m.group(1))) {
            for (int i = 1; i <= 7; i++) {
              if (relativeTo.plusDays(i).getDayOfWeek() == DateTimeUtils.asDay(m.group(2))) {
                offset = i;
                break;
              }
            }
          } else {
            for (int i = 1; i <= 7; i++) {
              if (relativeTo.minusDays(i).getDayOfWeek() == DateTimeUtils.asDay(m.group(2))) {
                offset = -i;
                break;
              }
            }
          }

          if (offset != null) createRelativeDay(content, m.start(), m.end(), offset, relativeTo);
        } else {
          createUnresolvedAnnotation(content, m.start(), m.end());
        }
      }
    }

    private void nextLastWeek(Text content, LocalDate relativeTo) {
      Pattern p =
          Pattern.compile(
              "\\b((in the|within the|" + DAYS + ") )?(next|last) week\\b",
              Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        if (m.group(3) != null) {
          if ("next".equalsIgnoreCase(m.group(4))) {
            createRelativeWeekDay(
                content, m.start(), m.end(), 1, DateTimeUtils.asDay(m.group(3)), relativeTo);
          } else {
            createRelativeWeekDay(
                content, m.start(), m.end(), -1, DateTimeUtils.asDay(m.group(3)), relativeTo);
          }
        } else if (m.group(2) != null) {
          if ("next".equalsIgnoreCase(m.group(4))) {
            createRelativeX(content, m.start(), m.end(), 1, ChronoUnit.WEEKS, relativeTo);
          } else {
            createRelativeX(content, m.start(), m.end(), -1, ChronoUnit.WEEKS, relativeTo);
          }
        } else {
          if ("next".equalsIgnoreCase(m.group(4))) {
            createRelativeWeek(content, m.start(), m.end(), 1, relativeTo);
          } else {
            createRelativeWeek(content, m.start(), m.end(), -1, relativeTo);
          }
        }
      }
    }

    private void nextLastMonth(Text content, LocalDate relativeTo) {
      Pattern p =
          Pattern.compile(
              "\\b((in the|within the) )?(next|last) month\\b", Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        if (m.group(2) != null) {
          if ("next".equalsIgnoreCase(m.group(3))) {
            createRelativeX(content, m.start(), m.end(), 1, ChronoUnit.MONTHS, relativeTo);
          } else {
            createRelativeX(content, m.start(), m.end(), -1, ChronoUnit.MONTHS, relativeTo);
          }
        } else {
          if ("next".equalsIgnoreCase(m.group(3))) {
            createRelativeMonth(content, m.start(), m.end(), 1, relativeTo);
          } else {
            createRelativeMonth(content, m.start(), m.end(), -1, relativeTo);
          }
        }
      }
    }

    private void nextLastYear(Text content, LocalDate relativeTo) {
      Pattern p =
          Pattern.compile(
              "\\b((in the|within the|" + MONTHS + ") )?(next|last) year\\b",
              Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        if (m.group(3) != null) {
          // e.g. October next year
          if ("next".equalsIgnoreCase(m.group(4))) {
            createRelativeYearMonth(
                content, m.start(), m.end(), 1, DateTimeUtils.asMonth(m.group(3)), relativeTo);
          } else {
            createRelativeYearMonth(
                content, m.start(), m.end(), -1, DateTimeUtils.asMonth(m.group(3)), relativeTo);
          }
        } else if (m.group(2) != null) {
          // e.g. in the next year
          if ("next".equalsIgnoreCase(m.group(4))) {
            createRelativeX(content, m.start(), m.end(), 1, ChronoUnit.YEARS, relativeTo);
          } else {
            createRelativeX(content, m.start(), m.end(), -1, ChronoUnit.YEARS, relativeTo);
          }
        } else {
          // e.g. next year
          if ("next".equalsIgnoreCase(m.group(4))) {
            createRelativeYear(content, m.start(), m.end(), 1, relativeTo);
          } else {
            createRelativeYear(content, m.start(), m.end(), -1, relativeTo);
          }
        }
      }
    }

    private void inTheNextLastX(Text content, LocalDate relativeTo) {
      Pattern p =
          Pattern.compile(
              "\\b(in|within) the (next|last) (\\d+) (day|week|month|year)s\\b",
              Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(content.getData());

      while (m.find()) {
        int offset = Integer.parseInt(m.group(3));
        if ("last".equalsIgnoreCase(m.group(2))) {
          offset = -offset;
        }

        if ("day".equalsIgnoreCase(m.group(4))) {
          createRelativeX(content, m.start(), m.end(), offset, ChronoUnit.DAYS, relativeTo);
        } else if ("week".equalsIgnoreCase(m.group(4))) {
          createRelativeX(content, m.start(), m.end(), offset, ChronoUnit.WEEKS, relativeTo);
        } else if ("month".equalsIgnoreCase(m.group(4))) {
          createRelativeX(content, m.start(), m.end(), offset, ChronoUnit.MONTHS, relativeTo);
        } else if ("year".equalsIgnoreCase(m.group(4))) {
          createRelativeX(content, m.start(), m.end(), offset, ChronoUnit.YEARS, relativeTo);
        }
      }
    }

    private void createRelativeX(
        Text content,
        int charBegin,
        int charEnd,
        int offset,
        TemporalUnit unit,
        LocalDate relativeTo) {
      if (relativeTo != null) {
        LocalDate d = relativeTo.plus(offset, unit);
        if (offset > 0) {
          createResolvedIntervalAnnotation(content, charBegin, charEnd, relativeTo, d, relativeTo);
        } else {
          createResolvedIntervalAnnotation(content, charBegin, charEnd, d, relativeTo, relativeTo);
        }
      } else {
        createUnresolvedAnnotation(content, charBegin, charEnd);
      }
    }

    private void createRelativeDay(
        Text content, int charBegin, int charEnd, int dayOffset, LocalDate relativeTo) {
      if (relativeTo != null) {
        LocalDate d = relativeTo.plusDays(dayOffset);

        createResolvedAnnotation(content, charBegin, charEnd, d, relativeTo);
      } else {
        createUnresolvedAnnotation(content, charBegin, charEnd);
      }
    }

    private void createRelativeWeek(
        Text content, int charBegin, int charEnd, int weekOffset, LocalDate relativeTo) {
      // Creates a week interval, starting on a Monday
      if (relativeTo != null) {
        LocalDate startOfWeek = relativeTo.plusWeeks(weekOffset);

        while (startOfWeek.getDayOfWeek() != DayOfWeek.MONDAY) {
          startOfWeek = startOfWeek.minusDays(1);
        }

        createResolvedIntervalAnnotation(
            content, charBegin, charEnd, startOfWeek, startOfWeek.plusWeeks(1), relativeTo);
      } else {
        createUnresolvedAnnotation(content, charBegin, charEnd);
      }
    }

    private void createRelativeWeekDay(
        Text content,
        int charBegin,
        int charEnd,
        int weekOffset,
        DayOfWeek day,
        LocalDate relativeTo) {

      if (relativeTo != null) {
        LocalDate dayOfWeek = relativeTo.plusWeeks(weekOffset);

        while (dayOfWeek.getDayOfWeek() != DayOfWeek.MONDAY) {
          dayOfWeek = dayOfWeek.minusDays(1);
        }

        while (dayOfWeek.getDayOfWeek() != day) {
          dayOfWeek = dayOfWeek.plusDays(1);
        }

        createResolvedAnnotation(content, charBegin, charEnd, dayOfWeek, relativeTo);
      } else {
        createUnresolvedAnnotation(content, charBegin, charEnd);
      }
    }

    private void createRelativeMonth(
        Text content, int charBegin, int charEnd, int monthOffset, LocalDate relativeTo) {
      if (relativeTo != null) {
        YearMonth ym = YearMonth.from(relativeTo).plusMonths(monthOffset);
        createResolvedAnnotation(content, charBegin, charEnd, ym, relativeTo);
      } else {
        createUnresolvedAnnotation(content, charBegin, charEnd);
      }
    }

    private void createRelativeYear(
        Text content, int charBegin, int charEnd, int yearOffset, LocalDate relativeTo) {

      if (relativeTo != null) {
        Year y = Year.from(relativeTo).plusYears(yearOffset);
        createResolvedAnnotation(content, charBegin, charEnd, y, relativeTo);
      } else {
        createUnresolvedAnnotation(content, charBegin, charEnd);
      }
    }

    private void createRelativeYearMonth(
        Text content,
        int charBegin,
        int charEnd,
        int yearOffset,
        Month month,
        LocalDate relativeTo) {

      if (relativeTo != null) {
        Year y = Year.from(relativeTo).plusYears(yearOffset);
        YearMonth ym = y.atMonth(month);

        createResolvedAnnotation(content, charBegin, charEnd, ym, relativeTo);
      } else {
        createUnresolvedAnnotation(content, charBegin, charEnd);
      }
    }

    private void createUnresolvedAnnotation(Text content, int charBegin, int charEnd) {
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(charBegin, charEnd))
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL)
          .save();
    }

    private void createResolvedAnnotation(
        Text content, int charBegin, int charEnd, Temporal t, LocalDate relativeTo) {
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(charBegin, charEnd))
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INSTANT)
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, t)
          .withProperty(PropertyKeys.PROPERTY_KEY_REFERENCE, relativeTo)
          .save();
    }

    @SuppressWarnings("rawtypes")
    private void createResolvedIntervalAnnotation(
        Text content,
        int charBegin,
        int charEnd,
        Temporal tStart,
        Temporal tEnd,
        LocalDate relativeTo) {
      Temporal start = tStart;
      Temporal end = tEnd;

      if (start instanceof ChronoLocalDate && end instanceof ChronoLocalDate) {
        ChronoLocalDate cStart = (ChronoLocalDate) start;
        ChronoLocalDate cEnd = (ChronoLocalDate) end;

        if (cStart.isAfter(cEnd)) {
          start = cEnd;
          end = cStart;
        }
      } else if (start instanceof ChronoZonedDateTime && end instanceof ChronoZonedDateTime) {
        ChronoZonedDateTime cStart = (ChronoZonedDateTime) start;
        ChronoZonedDateTime cEnd = (ChronoZonedDateTime) end;

        if (cStart.isAfter(cEnd)) {
          start = cEnd;
          end = cStart;
        }
      } else if (start instanceof ChronoLocalDateTime && end instanceof ChronoLocalDateTime) {
        ChronoLocalDateTime cStart = (ChronoLocalDateTime) start;
        ChronoLocalDateTime cEnd = (ChronoLocalDateTime) end;

        if (cStart.isAfter(cEnd)) {
          start = cEnd;
          end = cStart;
        }
      }
      // Else they're not the same type, so leave them as they are as we can't compare

      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(charBegin, charEnd))
          .withType(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL_INTERVAL)
          .withProperty(PropertyKeys.PROPERTY_KEY_START, tStart)
          .withProperty(PropertyKeys.PROPERTY_KEY_END, tEnd)
          .withProperty(PropertyKeys.PROPERTY_KEY_REFERENCE, relativeTo)
          .save();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {

    /**
     * The format of dates in the metadata fields
     *
     * <p>e.g. yyyy-MM-dd
     */
    private final String datePattern;

    /**
     * List of property names, in order of precedence, to use when looking for a date to make other
     * dates relative to
     *
     * <p>e.g. date,documentDate
     */
    private final Collection<String> dateProperties;

    private final boolean useTodayAsRelative;

    @JsonbCreator
    public Settings(
        @JsonbProperty("datePattern") String datePattern,
        @JsonbProperty("dateProperties") Collection<String> dateProperties,
        @JsonbProperty("useTodayAsRelative") boolean useTodayAsRelative) {
      this.datePattern = Objects.requireNonNullElse(datePattern, "yyyy-MM-dd");
      this.dateProperties = Objects.requireNonNullElse(dateProperties, List.of());
      this.useTodayAsRelative = useTodayAsRelative;
    }

    @Description(value = "The format of dates in the metadata fields", defaultValue = "yyyy-MM-dd")
    public String getDatePattern() {
      return datePattern;
    }

    public DateTimeFormatter dateFormatter() {
      return DateTimeFormatter.ofPattern(datePattern);
    }

    @Description(
        value =
            "List of field names, in order of precedence, to use when looking for a date to make other dates relative to",
        defaultValue = "")
    public Collection<String> getDateProperties() {
      return dateProperties;
    }

    @Description(
        value =
            "If true, then use today's date as the reference point for relative dates where an alternative can't be found in properties",
        defaultValue = "false")
    public boolean isUseTodayAsRelative() {
      return useTodayAsRelative;
    }

    @Override
    public boolean validate() {
      return datePattern != null && dateProperties != null;
    }
  }
}
