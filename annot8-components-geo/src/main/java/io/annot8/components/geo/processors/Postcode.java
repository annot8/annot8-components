/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.gov.dstl.geo.osgb.Constants;
import uk.gov.dstl.geo.osgb.EastingNorthingConversion;

import com.opencsv.CSVReader;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

@ComponentName("Postcode")
@ComponentDescription("Extract UK postcodes from text")
@SettingsClass(NoSettings.class)
public class Postcode extends AbstractProcessorDescriptor<Postcode.Processor, NoSettings> {
  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_ADDRESS, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractRegexProcessor {
    private static final String POSTCODE_REGEX =
        "\\b(GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKSTUW])|([A-Z-[IJZ]][0-9][ABEHMNPRVWXY])))) [0-9][A-Z-[CIKMOV]]{2})\\b";
    private Map<String, double[]> postcodeResource = new HashMap<>();

    public Processor() {
      super(Pattern.compile(POSTCODE_REGEX), 0, AnnotationTypes.ANNOTATION_TYPE_ADDRESS);
      initialisePostcodes();
    }

    @Override
    public void addProperties(Annotation.Builder builder, Matcher m) {
      String cleanPostcode = m.group().replaceAll("\\h+", "");
      if (postcodeResource.containsKey(cleanPostcode)) {
        double[] lonlat = postcodeResource.getOrDefault(cleanPostcode, null);

        if (lonlat != null && lonlat.length >= 2) {
          builder.withProperty(PropertyKeys.PROPERTY_KEY_LATITUDE, lonlat[0]);
          builder.withProperty(PropertyKeys.PROPERTY_KEY_LONGITUDE, lonlat[1]);
        }

        builder.withProperty("postcode", cleanPostcode);
      }
    }

    private void initialisePostcodes() {
      InputStream resourceAsStream = Processor.class.getResourceAsStream("ukpostcodes.csv");

      try (CSVReader reader = new CSVReader(new InputStreamReader(resourceAsStream)); ) {
        String[] line;
        while ((line = reader.readNext()) != null) {
          if (line.length < 3) {
            log().warn("Corrupt line found in ukpostcodes.csv - line will be skipped");
            continue;
          }
          double[] lonlat = parseEastingNorthingToLatLon(line[1], line[2]);
          postcodeResource.put(line[0].toUpperCase(), lonlat);
        }

        log().debug(postcodeResource.size() + " postcodes loaded from CSV");
      } catch (IOException e) {
        log().warn("Unable to load postcode data - geospatial data will not be available", e);
      }
    }

    private double[] parseEastingNorthingToLatLon(String easting, String northing) {
      return EastingNorthingConversion.toLatLon(
          new double[] {Double.parseDouble(easting), Double.parseDouble(northing)},
          Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
          Constants.ELLIPSOID_AIRY1830_MINORAXIS,
          Constants.NATIONALGRID_N0,
          Constants.NATIONALGRID_E0,
          Constants.NATIONALGRID_F0,
          Constants.NATIONALGRID_LAT0,
          Constants.NATIONALGRID_LON0);
    }
  }
}
