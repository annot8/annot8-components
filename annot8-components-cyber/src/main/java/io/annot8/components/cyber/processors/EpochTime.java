/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.api.annotations.Annotation.Builder;
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
import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Epoch Time")
@ComponentDescription("Extract epoch time in either seconds or milliseconds, with optional start and end dates")
@SettingsClass(EpochTime.Settings.class)
public class EpochTime extends AbstractProcessorDescriptor<EpochTime.Processor, EpochTime.Settings> {
  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TIMESTAMP, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {

    private Settings settings;

    public Processor(Settings settings) {
      super(
          Pattern.compile("\\b\\d+\\b", Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_TIMESTAMP);
      this.settings = settings;
    }

    @Override
    protected boolean acceptMatch(Matcher m) {
      long l;
      try {
        l = Long.parseLong(m.group());
      } catch (NumberFormatException nfe) {
        return false;
      }

      Instant i;
      if (settings.isMilliseconds()) {
        i = Instant.ofEpochMilli(l);
      } else {
        i = Instant.ofEpochSecond(l);
      }

      return (i.isAfter(settings.getEarliestTimestamp()) || i.equals(settings.getEarliestTimestamp()))
          && (i.isBefore(settings.getLatestTimestamp()) || i.equals(settings.getLatestTimestamp()));
    }

    @Override
    protected void addProperties(Builder builder, Matcher m) {
      if (settings.isMilliseconds()) {
        builder.withProperty(PropertyKeys.PROPERTY_KEY_UNIT, "ms");
        builder.withProperty(PropertyKeys.PROPERTY_KEY_REFERENCE, "1970-01-01T00:00:00.000Z");
      } else {
        builder.withProperty(PropertyKeys.PROPERTY_KEY_UNIT, "s");
        builder.withProperty(PropertyKeys.PROPERTY_KEY_REFERENCE, "1970-01-01T00:00:00Z");
      }
    }

  }

  public static class Settings implements io.annot8.api.settings.Settings {

    private Instant earliestTimestamp = Instant.MIN;
    private Instant latestTimestamp = Instant.MAX;
    private boolean milliseconds = false;

    @Override
    public boolean validate() {
      return earliestTimestamp.isBefore(latestTimestamp)
          || earliestTimestamp.equals(latestTimestamp);
    }

    @Description("The earliest timestamp to accept - timestamps before this will be ignored")
    public Instant getEarliestTimestamp() {
      return earliestTimestamp;
    }

    public void setEarliestTimestamp(Instant earliestTimestamp) {
      this.earliestTimestamp = earliestTimestamp;
    }

    @Description("The latest timestamp to accept - timestamps after this will be ignored")
    public Instant getLatestTimestamp() {
      return latestTimestamp;
    }

    public void setLatestTimestamp(Instant latestTimestamp) {
      this.latestTimestamp = latestTimestamp;
    }

    @Description("Whether timestamps are in milliseconds (true) or seconds (false)")
    public boolean isMilliseconds() {
      return milliseconds;
    }

    public void setMilliseconds(boolean milliseconds) {
      this.milliseconds = milliseconds;
    }
  }
}
