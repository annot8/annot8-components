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
import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Annotate times within a document using regular expressions
 *
 * <p>The document content is run through a regular expression matcher looking for things that match
 * the following time regular expression, where UTC is being used to represent all time zone
 * acronyms defined in Java:
 *
 * <pre>
 * \\b(((0?[0-9])|([0-9]{2}))[:][0-9]{2}\\h*((UTC)([ ]?[+-][ ]?((0?[0-9])|(1[0-2])))?)?\\h*(pm|am)?)\\b|\\b(((1[0-2])|([1-9]))(pm|am))\\b|\\b(midnight)\\b|\\b(midday)\\b|\\b((12\\h)?noon)\\b|\\b([0-2][0-9][0-5][0-9][ ]?(hr(s)?)?[ ]?((UTC)([ ]?[+-][ ]?((0?[0-9])|(1[0-2])))?)?)\\b
 * </pre>
 *
 * <p>This will only capture times that match the regular expression, and will miss times expressed
 * in a different format. By default, only times that contain alphabetical characters or colons will
 * be accepted to minimise false positives.
 */
@ComponentName("Time") // The display name of the processor
@ComponentDescription("Extracts formatted times from text")
@SettingsClass(Time.Settings.class)
public class Time extends AbstractProcessorDescriptor<Time.Processor, Time.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getRequireAlpha());
  }

  public static class Processor extends AbstractRegexProcessor {

    private Boolean requireAlpha;

    private static final String TIME_ZONES =
        Arrays.stream(TimeZone.getAvailableIDs())
            .filter(s -> s.length() <= 3)
            .filter(s -> s.equals(s.toUpperCase()))
            .collect(Collectors.joining("|"));

    private static final String TIME_REGEX =
        "\\b(([0-1]?[0-9]|2[0-4])[:.][0-5][0-9]\\h*(("
            + TIME_ZONES
            + ")([ ]?[+-][ ]?((0?[0-9])|(1[0-2])))?)?\\h*(pm|am)?)\\b|\\b(((1[0-2])|([1-9]))(pm|am))\\b|\\b(midnight)\\b|\\b(midday)\\b|\\b((12\\h)?noon)\\b|\\b([0-1][0-9]|2[0-4])[0-5][0-9][ ]?(((hr(s)?)?[ ]?(("
            + TIME_ZONES
            + ")([ ]?[+-][ ]?((0?[0-9])|(1[0-2])))?)?)|hours|h)\\b";

    public Processor(boolean requireAlpha) {

      super(Pattern.compile(TIME_REGEX), 0, AnnotationTypes.ANNOTATION_TYPE_TEMPORAL);
      this.requireAlpha = requireAlpha;
    }

    @Override
    protected boolean acceptMatch(Matcher matcher) {

      if (requireAlpha) {
        String time = matcher.group();
        return time.matches(".*[a-zA-Z:].*");
      }

      return true;
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private boolean requireAlpha = true;
    /**
     * Do we require that there are alphabetical characters in the time? This helps avoid picking
     * out things like 2015 as a time when it should be a year, as it forces the time to be written
     * like 2015hrs or 8:15pm.
     *
     * <p>For the purposes of the TimeRegex annotator, colons are treated as alphabetical
     * characters, such that times such as 20:15 are captured. Other punctuation isn't, as 20.15 is
     * more like to be an amount than a time.
     */
    @Description("Do we require that there are alphabetical characters or colons in the time?")
    public boolean getRequireAlpha() {
      return requireAlpha;
    }

    public void setRequireAlpha(boolean requireAlpha) {
      this.requireAlpha = requireAlpha;
    }

    @Override
    public boolean validate() {
      // invalid settings are not possible
      return true;
    }
  }
}
