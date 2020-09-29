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
import io.annot8.components.base.text.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Unqualified Date") // The display name of the processor
@ComponentDescription(
    "Extracts unqualified dates from text and annotates them as Temporal entities.")
@SettingsClass(UnqualifiedDate.Settings.class)

/**
 * Extracts unqualified dates from text and annotates them as Temporal entities. We take an
 * unqualified date to be any date without a year for the purposes of this annotator.
 */
public class UnqualifiedDate
    extends AbstractProcessorDescriptor<UnqualifiedDate.Processor, UnqualifiedDate.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_TEMPORAL, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getAllowLowercase());
  }

  public static class Processor extends AbstractRegexProcessor {

    private Boolean allowLowercase;

    private static final String DAYS =
        "(Mon(day)?+|Tue(s(day)?+)?+|Wed(nesday)?+|Thu(r(s(day)?+)?+)?+|Fri(day)?+|Sat(urday)?+|Sun(day)?+)";
    private static final String SUFFIXES = "(st|nd|rd|th)";
    private static final String MONTHS =
        "(Jan(uary)?+|Feb(ruary)?+|Mar(ch)?+|Apr(il)?+|May|Jun(e)?+|Jul(y)?+|Aug(ust)?+|Sep(t(ember)?+)?+|Oct(ober)?+|Nov(ember)?+|Dec(ember)?+)";

    private static final String PATTERN =
        "\\b(("
            + DAYS
            + " )?((([1-9]|[12][0-9]|3[01])"
            + SUFFIXES
            + "?+ (?:of )?"
            + MONTHS
            + "|"
            + MONTHS
            + " ([1-9]|[12][0-9]|3[01])"
            + SUFFIXES
            + "?+|"
            + MONTHS
            + "|([1-9]|[12][0-9]|3[01])"
            + SUFFIXES
            + ")+)|"
            + DAYS
            + " ?)\\b(\\s*(\\d{4}|'?\\d{2}))?";

    public Processor(boolean allowLowerCase) {

      super(
          Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE),
          0,
          AnnotationTypes.ANNOTATION_TYPE_TEMPORAL);
      this.allowLowercase = allowLowerCase;
    }

    @Override
    protected boolean acceptMatch(Matcher matcher) {

      if (matcher.group(72) != null) {
        return false;
      }

      return allowLowercase
          || (startsWithCapital(matcher.group(2))
              && startsWithCapital(matcher.group(18))
              && startsWithCapital(matcher.group(31))
              && startsWithCapital(matcher.group(46))
              && startsWithCapital(matcher.group(61)));
    }

    /** Returns true if the String s starts with a capital letter */
    public static boolean startsWithCapital(String s) {
      if (s == null || s.length() == 0) return true;

      String letter = s.substring(0, 1);
      return letter.toUpperCase().equals(letter);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {

    private boolean allowLowercase = false;

    /**
     * Extracts unqualified dates from text and annotates them as Temporal entities. We take an
     * unqualified date to be any date without a year for the purposes of this annotator.
     */
    @Description("Allow lower case letters for months and days?")
    public boolean getAllowLowercase() {
      return allowLowercase;
    }

    public void setAllowLowercase(boolean allowLowercase) {
      this.allowLowercase = allowLowercase;
    }

    @Override
    public boolean validate() {
      // invalid settings are not possible
      return true;
    }
  }
}
