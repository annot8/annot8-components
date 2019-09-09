/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opensextant.geodesy.MGRS;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.capabilities.CreatesAnnotation;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.Settings;
import io.annot8.core.settings.SettingsClass;

/** Extract MGRS coordinates, optionally ignoring MGRS coordinates that could be dates */
@CreatesAnnotation(value = AnnotationTypes.ANNOTATION_TYPE_COORDINATE, bounds = SpanBounds.class)
@SettingsClass(Mgrs.MgrsSettings.class)
public class Mgrs extends AbstractTextProcessor {
  private static final Pattern mgrsPattern =
      Pattern.compile(
          "\\b(([1-9]|[1-5][0-9]|60)\\h*([C-HJ-NP-X])\\h*[A-HJ-NP-Z][A-HJ-NP-V]\\h*(([0-9]{5}\\h*[0-9]{5})|([0-9]{4}\\h*[0-9]{4})|([0-9]{3}\\h*[0-9]{3})|([0-9]{2}\\h*[0-9]{2})|([0-9]\\h*[0-9])))\\b");
  private static final Pattern datesPattern =
      Pattern.compile(
          "([0-2]?[0-9]|3[01])\\h*(JAN|FEB|MAR|JUN|JUL|SEP|DEC)\\h*([0-9]{2}|[0-9]{4})");

  private MgrsSettings settings;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    settings = context.getSettings(MgrsSettings.class, new MgrsSettings(false));
  }

  @Override
  protected void process(Item item, Text content) {
    Matcher m = mgrsPattern.matcher(content.getData());

    while (m.find()) {
      String coordinates = m.group();
      if (settings.isIgnoreDates()) {
        Matcher mDates = datesPattern.matcher(coordinates);
        if (mDates.matches()) {
          log().info("Discarding possible MGRS coordinate {} as it resembles a date", coordinates);
          continue;
        }
      }

      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(m.start(), m.end()))
          .withType(AnnotationTypes.ANNOTATION_TYPE_COORDINATE)
          .withProperty(PropertyKeys.PROPERTY_KEY_COORDINATETYPE, "MGRS")
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, coordinates.replaceAll("\\h+", ""))
          .withPropertyIfPresent(PropertyKeys.PROPERTY_KEY_GEOJSON, getGeoJson(coordinates))
          .save();
    }
  }

  private Optional<String> getGeoJson(String coordinate) {
    try {
      MGRS mgrs = new MGRS(coordinate);
      return Optional.of(
          "{\"type\":\"Polygon\",\"coordinates\":[["
              + "["
              + mgrs.getBoundingBox().getEastLon().inDegrees()
              + ","
              + mgrs.getBoundingBox().getNorthLat().inDegrees()
              + "],"
              + "["
              + mgrs.getBoundingBox().getWestLon().inDegrees()
              + ","
              + mgrs.getBoundingBox().getNorthLat().inDegrees()
              + "],"
              + "["
              + mgrs.getBoundingBox().getWestLon().inDegrees()
              + ","
              + mgrs.getBoundingBox().getSouthLat().inDegrees()
              + "],"
              + "["
              + mgrs.getBoundingBox().getEastLon().inDegrees()
              + ","
              + mgrs.getBoundingBox().getSouthLat().inDegrees()
              + "],"
              + "["
              + mgrs.getBoundingBox().getEastLon().inDegrees()
              + ","
              + mgrs.getBoundingBox().getNorthLat().inDegrees()
              + "]]]}");
    } catch (IllegalArgumentException e) {
      log().warn("Couldn't parse MGRS co-ordinate", e);
    }

    return Optional.empty();
  }

  public static class MgrsSettings implements Settings {
    private final boolean ignoreDates;

    public MgrsSettings() {
      this.ignoreDates = false;
    }

    public MgrsSettings(boolean ignoreDates) {
      this.ignoreDates = ignoreDates;
    }

    public boolean isIgnoreDates() {
      return ignoreDates;
    }

    @Override
    public boolean validate() {
      return true;
    }
  }
}
