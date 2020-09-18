/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

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
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("LatLon")
@ComponentDescription("Extract latitude-longitude pairs from text")
@SettingsClass(LatLon.Settings.class)
public class LatLon extends AbstractProcessorDescriptor<LatLon.Processor, LatLon.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.isLonLat(), settings.getMinDP());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private final Pattern llDMSPattern =
        Pattern.compile(
            "\\b(\\d{1,3})°(\\d{1,2})'(\\d{1,2}(\\.\\d+)?)\"([NSEW])[,/\\h]*(\\d{1,3})°(\\d{1,2})'(\\d{1,2}(\\.\\d+)?)\"([NSEW])\\b");
    private final Pattern llDMSSpacePattern =
        Pattern.compile(
            "\\b(\\d{1,3}) (\\d{1,2}) (\\d{1,2}(\\.\\d+)?) ([NSEW])[,/\\h]*(\\d{1,3}) (\\d{1,2}) (\\d{1,2}(\\.\\d+)?) ([NSEW])\\b");
    private final Pattern llDMSNumericPattern =
        Pattern.compile(
            "\\b(\\d{2,3})(\\d{2})(\\d{2})?( )?([NSEW])[,/\\h]*(\\d{2,3})(\\d{2})(\\d{2})?( )?([NSEW])\\b");
    private final Pattern llDMSPunctuationPattern =
        Pattern.compile(
            "\\b(\\d{2,3})-(\\d{2}),(\\d{2})?( )?([NSEW])[,/\\h]*(\\d{2,3})-(\\d{2}),(\\d{2})?( )?([NSEW])\\b");

    private final Pattern llDMSTextPattern =
        Pattern.compile(
            "\\b((lat|latitude)\\h*)?(\\d{1,2})°\\h*(\\d{1,2}(\\.\\d+)?)'(\\h*(\\d{1,2}(\\.\\d+)?)\")?\\h*([NS])\\.?,?\\h*(lon|long|longitude)?\\h*(\\d{1,3})°\\h*(\\d{1,2}(\\.\\d+)?)'(\\h*(\\d{1,2}(\\.\\d+)?)\")?\\h*([EW])\\b",
            Pattern.CASE_INSENSITIVE);
    private final Pattern llDMTextPattern =
        Pattern.compile(
            "\\b((lat|latitude)\\h*)?(\\d{1,2})°\\h*(\\d{1,2})'\\.(\\d+)\\h*([NS])\\.?,?\\h*(lon|long|longitude)?\\h*(\\d{1,3})°\\h*(\\d{1,2})'\\.(\\d+)\\h*([EW])\\b",
            Pattern.CASE_INSENSITIVE);

    /** Variable to hold the regular expression pattern for Digital Degrees */
    private final Pattern llDDPattern;

    /** Variable to hold the regular expression pattern for Digital Degrees with degree symbol */
    private final Pattern llDDSymPattern;

    /**
     * Variable to hold the regular expression pattern for Digital Degrees with cardinal (NSEW)
     * symbols
     */
    private final Pattern llDDCardPattern;

    private final boolean lonLat;

    private final List<String> currencySymbols = Arrays.asList("£", "$", "€");

    private static final String COULD_NOT_PARSE =
        "Couldn't parse extracted coordinates - coordinate will be skipped";

    public Processor(boolean lonLat, int minDP) {
      this.lonLat = lonLat;

      if (minDP == 0) {
        // No word boundary characters as that excludes negative signs
        llDDPattern = Pattern.compile("(-?\\d{1,3}(\\.\\d+)?)(,\\h*|\\h+)(-?\\d{1,3}(\\.\\d+)?)");
        llDDSymPattern =
            Pattern.compile("(-?\\d{1,3}(\\.\\d+)?)°(,\\h*|\\h+)(-?\\d{1,3}(\\.\\d+)?)°");
        llDDCardPattern =
            Pattern.compile(
                "\\b(\\d{1,3}(\\.\\d+)?)°( )?([NSEW])(,\\h*|\\h+)(\\d{1,3}(\\.\\d+)?)°( )?([NSEW])");
      } else {
        llDDPattern =
            Pattern.compile(
                "(-?\\d{1,3}(\\.\\d{"
                    + minDP
                    + ",}))(,\\h*|\\h+)(-?\\d{1,3}(\\.\\d{"
                    + minDP
                    + ",}))");
        llDDSymPattern =
            Pattern.compile(
                "(-?\\d{1,3}(\\.\\d{"
                    + minDP
                    + ",}))°(,\\h*|\\h+)(-?\\d{1,3}(\\.\\d{"
                    + minDP
                    + ",}))°");
        llDDCardPattern =
            Pattern.compile(
                "\\b(\\d{1,3}(\\.\\d{"
                    + minDP
                    + ",}))°( )?([NSEW])(,\\h*|\\h+)(\\d{1,3}(\\.\\d{"
                    + minDP
                    + ",}))°( )?([NSEW])");
      }
    }

    @Override
    protected void process(Text content) {
      Set<String> found = new HashSet<>();
      String text = normalizeQuotesAndDots(content.getData());

      processDD(content, text, found);
      processDDCard(content, text, found);
      processDMS(content, text, found);
      processDMSText(content, text, found);
    }

    /**
     * Searches the text for digital degree strings This method handles digital degree strings
     * consisting of a pair of decimal numbers separated by whitespace or a comma and optionally
     * with the degree symbol. The pair is assumed to be in the order Latitude, Longitude unless the
     * variable lonLat has been set to true.
     */
    private void processDD(Text content, String text, Set<String> found) {
      Pattern[] patterns = new Pattern[] {llDDPattern, llDDSymPattern};
      for (Pattern p : patterns) {
        Matcher matcher = p.matcher(text);

        while (matcher.find()) {
          if (currencySymbols.contains(text.substring(matcher.start(1) - 1, matcher.start(1)))) {
            log().info("Skipping coordinate as it is preceded by a currency symbol");
            continue;
          }

          try {
            double lat;
            double lon;

            if (!lonLat) {
              lat = Double.parseDouble(matcher.group(1));
              lon = Double.parseDouble(matcher.group(4));
            } else {
              lon = Double.parseDouble(matcher.group(1));
              lat = Double.parseDouble(matcher.group(4));
            }

            addCoordinate(content, matcher.start(), matcher.end(), lon, lat, "dd", found);

          } catch (NumberFormatException e) {
            log().warn(COULD_NOT_PARSE, e);
          }
        }
      }
    }

    /**
     * Searches the text for digital degree strings with cardinal points. This method handles
     * processing of digital degree strings consisting of a pair of decimal numbers separated by
     * whitespace or a comma and each number is followed by a degree symbol and a letter
     * representing its cardinal compass point, i.e. N, S, E, W. The cardinal point is used to
     * determine which value is latitude and which is longitude.
     */
    private void processDDCard(Text content, String text, Set<String> found) {

      Matcher matcher = llDDCardPattern.matcher(text);
      while (matcher.find()) {
        // If no valid cardinal point letter then skip it
        if (isInvalidPair(matcher.group(4), matcher.group(9))) {
          continue;
        }

        try {
          Double lat;
          Double lon;

          // Assume latitude first
          lat = Double.parseDouble(matcher.group(1));
          lon = Double.parseDouble(matcher.group(6));
          if ("E".equals(matcher.group(4)) || "W".equals(matcher.group(4))) {
            // Actually longitude first so swap values
            Double tmp = lat;
            lat = lon;
            lon = tmp;
          }

          if (flipLon(matcher.group(4), matcher.group(9))) {
            lon = -lon;
          }

          if (flipLat(matcher.group(4), matcher.group(9))) {
            lat = -lat;
          }

          addCoordinate(content, matcher.start(), matcher.end(), lon, lat, "dd", found);

        } catch (NumberFormatException e) {
          log().warn(COULD_NOT_PARSE, e);
        }
      }
    }

    private void processDMS(Text content, String text, Set<String> found) {

      Pattern[] patterns =
          new Pattern[] {
            llDMSPattern, llDMSSpacePattern, llDMSNumericPattern, llDMSPunctuationPattern
          };

      for (Pattern p : patterns) {
        Matcher matcher = p.matcher(text);

        while (matcher.find()) {
          if (isInvalidPair(matcher.group(5), matcher.group(10))) {
            continue;
          }

          try {
            double[] lonLat = determineLonLatDMS(matcher);
            addCoordinate(
                content, matcher.start(), matcher.end(), lonLat[0], lonLat[1], "dms", found);
          } catch (NumberFormatException e) {
            log().warn(COULD_NOT_PARSE, e);
          }
        }
      }
    }

    private void processDMSText(Text content, String text, Set<String> found) {

      Matcher m = llDMSTextPattern.matcher(text);
      while (m.find()) {
        double lat = Double.parseDouble(m.group(3));
        lat += Double.parseDouble(m.group(4)) / 60;
        if (m.group(7) != null) lat += Double.parseDouble(m.group(7)) / 3600;
        if ("S".equalsIgnoreCase(m.group(9))) lat = -lat;

        double lon = Double.parseDouble(m.group(11));
        lon += Double.parseDouble(m.group(12)) / 60;
        if (m.group(15) != null) lon += Double.parseDouble(m.group(15)) / 3600;
        if ("W".equalsIgnoreCase(m.group(17))) lon = -lon;

        addCoordinate(content, m.start(), m.end(), lon, lat, "dms", found);
      }

      m = llDMTextPattern.matcher(text);
      while (m.find()) {
        double lat = Double.parseDouble(m.group(3));
        lat += Double.parseDouble(m.group(4)) / 60;
        lat += Double.parseDouble(m.group(5)) / 3600;
        if ("S".equalsIgnoreCase(m.group(6))) lat = -lat;

        double lon = Double.parseDouble(m.group(8));
        lon += Double.parseDouble(m.group(9)) / 60;
        lon += Double.parseDouble(m.group(10)) / 3600;
        if ("S".equalsIgnoreCase(m.group(11))) lon = -lon;

        addCoordinate(content, m.start(), m.end(), lon, lat, "dms", found);
      }
    }

    private double[] determineLonLatDMS(Matcher matcher) {
      double lat =
          dmsToDeg(
              Integer.parseInt(matcher.group(1)),
              Integer.parseInt(matcher.group(2)),
              parseOrNull(matcher.group(3)));

      double lon =
          dmsToDeg(
              Integer.parseInt(matcher.group(6)),
              Integer.parseInt(matcher.group(7)),
              parseOrNull(matcher.group(8)));

      if ("E".equals(matcher.group(5)) || "W".equals(matcher.group(5))) {
        Double tmp = lat;
        lat = lon;
        lon = tmp;
      }

      if (flipLon(matcher.group(5), matcher.group(10))) {
        lon = -lon;
      }

      if (flipLat(matcher.group(5), matcher.group(10))) {
        lat = -lat;
      }

      return new double[] {lon, lat};
    }

    /**
     * Determines whether we have both a North/South and an East/West directional indicator present
     */
    private boolean isInvalidPair(String... parameters) {
      boolean nFound = false;
      boolean eFound = false;

      for (String s : parameters) {
        if ("N".equalsIgnoreCase(s) || "S".equalsIgnoreCase(s)) {
          nFound = true;
        } else if ("E".equalsIgnoreCase(s) || "W".equalsIgnoreCase(s)) {
          eFound = true;
        }
      }

      return !nFound || !eFound;
    }

    private boolean flipLat(String... parameters) {
      for (String s : parameters) {
        if ("S".equalsIgnoreCase(s)) {
          return true;
        }
      }

      return false;
    }

    private boolean flipLon(String... parameters) {
      for (String s : parameters) {
        if ("W".equalsIgnoreCase(s)) {
          return true;
        }
      }

      return false;
    }

    /**
     * Converts a Degrees Minutes Seconds coordinate into a decimal degree value The conversion is
     * ignorant of the cardinality (N, S, E, W) so degrees value should be positive and the
     * conversion of an S latitude or W longitude coordinate to a negative value should be carried
     * out by the calling function.
     *
     * @param d number of degrees. It is assumed this is a positive value
     * @param m number of minutes
     * @param s number of seconds, or null if no seconds supplied
     * @return the decimal degree value for the degrees minutes seconds
     */
    private double dmsToDeg(Integer d, Integer m, Double s) {
      double seconds = m * 60.0;
      if (s != null) {
        seconds += s;
      }
      return d + (seconds / 3600);
    }

    private Double parseOrNull(String s) {
      if (s != null) {
        return Double.parseDouble(s);
      } else {
        return null;
      }
    }

    private void addCoordinate(
        Text content,
        int begin,
        int end,
        double lon,
        double lat,
        String coordinateType,
        Set<String> found) {
      if (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180) {
        String textLoc = begin + "," + end;

        if (found.add(textLoc)) {
          String coords = "[" + lon + "," + lat + "]";

          content
              .getAnnotations()
              .create()
              .withBounds(new SpanBounds(begin, end))
              .withType(AnnotationTypes.ANNOTATION_TYPE_COORDINATE)
              .withProperty(PropertyKeys.PROPERTY_KEY_COORDINATETYPE, coordinateType)
              .withProperty(
                  PropertyKeys.PROPERTY_KEY_GEOJSON,
                  "{\"type\":\"Point\",\"coordinates\":" + coords + "}")
              .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, lat + ";" + lon)
              .withProperty(PropertyKeys.PROPERTY_KEY_LONGITUDE, lon)
              .withProperty(PropertyKeys.PROPERTY_KEY_LATITUDE, lat)
              .save();
        }
      }
    }

    /**
     * Replace smart quotes, curly quotes, back ticks and mid-dots with standard quotes and dots to
     * simplify the required regular expressions.
     *
     * <p>Symbols are replaced with the same number of characters (one-to-one replacement) so that
     * character offsets are not affected.
     */
    public static String normalizeQuotesAndDots(String s) {
      return s.replaceAll("[\\u201C\\u201D\\u2033\\u02BA\\u301E\\u3003]", "\"")
          .replaceAll("[\\u2018\\u2019\\u2032\\u00B4\\u02B9`]", "'")
          .replaceAll("[\\u00B7]", ".");
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {

    private boolean lonLat = false;
    private int minDP = 2;

    @Override
    public boolean validate() {
      return true;
    }

    @Description("Is the order of coordinates Longitude first (true) or Latitude first (false)")
    public boolean isLonLat() {
      return lonLat;
    }

    public void setLonLat(boolean lonLat) {
      this.lonLat = lonLat;
    }

    @Description("The minimum number of decimal places required when parsing decimal degrees")
    public int getMinDP() {
      return minDP;
    }

    public void setMinDP(int minDP) {
      this.minDP = minDP;
    }
  }
}
