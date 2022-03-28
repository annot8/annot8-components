/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

@ComponentName("Item Property as Date Time")
@ComponentDescription("Converts an existing Item Property into a date time")
@SettingsClass(ItemPropertyAsDateTime.Settings.class)
@ComponentTags({"item", "properties", "temporal", "date", "time"})
public class ItemPropertyAsDateTime
    extends AbstractProcessorDescriptor<
        ItemPropertyAsDateTime.Processor, ItemPropertyAsDateTime.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;
    private final DateTimeFormatter dateTimeFormatter;

    public Processor(Settings settings) {
      this.settings = settings;
      try {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(settings.getDateTimeFormat());
      } catch (IllegalArgumentException e) {
        throw new BadConfigurationException("Provided date time format could not be parsed", e);
      }
    }

    @Override
    public ProcessorResponse process(Item item) {
      Optional<Object> value = item.getProperties().get(settings.getKey());
      if (value.isPresent()) {
        Object o = value.get();

        Temporal temporal = null;
        if (o instanceof TemporalAccessor) {
          try {
            switch (settings.getDateTimeType()) {
              case DATE:
                temporal = LocalDate.from((TemporalAccessor) o);
                break;
              case TIME:
                temporal = LocalTime.from((TemporalAccessor) o);
                break;
              case DATETIME:
                temporal = LocalDateTime.from((TemporalAccessor) o);
                break;
              case OFFSET_DATETIME:
                temporal = OffsetDateTime.from((TemporalAccessor) o);
                break;
              case ZONED_DATETIME:
                temporal = ZonedDateTime.from((TemporalAccessor) o);
                break;
            }
          } catch (DateTimeException e) {
            log()
                .warn(
                    "Could not convert property {} from {} ({})",
                    settings.getKey(),
                    o.getClass().getName(),
                    e.getMessage());

            if (settings.isErrorOnUnparseable()) return ProcessorResponse.itemError(e);
          }
        } else {
          String s = o.toString();

          try {
            switch (settings.getDateTimeType()) {
              case DATE:
                temporal = LocalDate.parse(s, dateTimeFormatter);
                break;
              case TIME:
                temporal = LocalTime.parse(s, dateTimeFormatter);
                break;
              case DATETIME:
                temporal = LocalDateTime.parse(s, dateTimeFormatter);
                break;
              case ZONED_DATETIME:
                temporal = ZonedDateTime.parse(s, dateTimeFormatter);
                break;
              case OFFSET_DATETIME:
                temporal = OffsetDateTime.parse(s, dateTimeFormatter);
                break;
            }
          } catch (DateTimeParseException e) {
            log()
                .warn(
                    "Could not parse {} from property {} ({})",
                    s,
                    settings.getKey(),
                    e.getMessage());

            if (settings.isErrorOnUnparseable()) return ProcessorResponse.itemError(e);
          }
        }

        if (temporal != null) {
          item.getProperties().set(settings.getKey(), temporal);
        } else {
          item.getProperties().remove(settings.getKey());
        }
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String key = "date";
    private String dateTimeFormat = "yyyy-MM-dd";
    private DateTimeType dateTimeType = DateTimeType.DATETIME;
    private boolean errorOnUnparseable = true;

    @Override
    public boolean validate() {
      return key != null && dateTimeFormat != null && dateTimeType != null;
    }

    @Description("The property key to convert")
    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    @Description("The format to use when parsing")
    public String getDateTimeFormat() {
      return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
      this.dateTimeFormat = dateTimeFormat;
    }

    @Description("The type of date time to create")
    public DateTimeType getDateTimeType() {
      return dateTimeType;
    }

    public void setDateTimeType(DateTimeType dateTimeType) {
      this.dateTimeType = dateTimeType;
    }

    @Description(
        "If false, then dates that can't be parsed will be discarded. If true, then unparseable dates will cause an item error to be returned.")
    public boolean isErrorOnUnparseable() {
      return errorOnUnparseable;
    }

    public void setErrorOnUnparseable(boolean errorOnUnparseable) {
      this.errorOnUnparseable = errorOnUnparseable;
    }
  }

  public enum DateTimeType {
    DATE,
    TIME,
    DATETIME,
    ZONED_DATETIME,
    OFFSET_DATETIME
  }
}
