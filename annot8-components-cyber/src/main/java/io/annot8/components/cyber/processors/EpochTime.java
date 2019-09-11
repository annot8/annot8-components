/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.components.base.processors.AbstractRegex;
import io.annot8.components.cyber.processors.EpochTime.EpochTimeSettings;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.annotations.Annotation.Builder;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.Settings;

import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpochTime extends AbstractRegex<EpochTimeSettings> {

  private EpochTimeSettings settings = new EpochTimeSettings();

  public EpochTime() {
    super(
        Pattern.compile("\\b\\d+\\b", Pattern.CASE_INSENSITIVE),
        0,
        AnnotationTypes.ANNOTATION_TYPE_TIMESTAMP);
  }

  @Override
  public void configure(EpochTimeSettings settings) throws BadConfigurationException, MissingResourceException {
    this.settings = settings;
  }

  @Override
  protected boolean acceptMatch(Matcher m) {
    Long l;
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

  public static class EpochTimeSettings implements Settings {

    private Instant earliestTimestamp = Instant.MIN;
    private Instant latestTimestamp = Instant.MAX;
    private boolean milliseconds = false;

    @Override
    public boolean validate() {
      return earliestTimestamp.isBefore(latestTimestamp)
          || earliestTimestamp.equals(latestTimestamp);
    }

    public Instant getEarliestTimestamp() {
      return earliestTimestamp;
    }

    public void setEarliestTimestamp(Instant earliestTimestamp) {
      this.earliestTimestamp = earliestTimestamp;
    }

    public Instant getLatestTimestamp() {
      return latestTimestamp;
    }

    public void setLatestTimestamp(Instant latestTimestamp) {
      this.latestTimestamp = latestTimestamp;
    }

    public boolean isMilliseconds() {
      return milliseconds;
    }

    public void setMilliseconds(boolean milliseconds) {
      this.milliseconds = milliseconds;
    }
  }
}
