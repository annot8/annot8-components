/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.annot8.components.geo.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import uk.gov.dstl.geo.osgb.Constants;
import uk.gov.dstl.geo.osgb.EastingNorthingConversion;
import uk.gov.dstl.geo.osgb.NationalGrid;
import uk.gov.dstl.geo.osgb.OSGB36;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ComponentName("Ordnance Survey Coordinates")
@ComponentDescription("Extract  6, 8 or 10 figure OS coordinates within a document")
public class OSGB extends AbstractProcessorDescriptor<OSGB.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private static final Pattern OSGB_PATTERN = Pattern.compile("\\b([HJNOST][A-HJ-Z])( )?([0-9]{6}|[0-9]{3} [0-9]{3}|[0-9]{8}|[0-9]{4} [0-9]{4}|[0-9]{10}|[0-9]{5} [0-9]{5})\\b", Pattern.CASE_INSENSITIVE);

    public Processor() {

    }

    @Override
    protected void process(Text content) {
      Matcher m = OSGB_PATTERN.matcher(content.getData());

      while (m.find()) {
        try {
          // Attempt to convert to a lat lon
          double[] en = NationalGrid.fromNationalGrid(m.group());
          double[] latlonOSGB38 =
              EastingNorthingConversion.toLatLon(
                  en,
                  Constants.ELLIPSOID_AIRY1830_MAJORAXIS,
                  Constants.ELLIPSOID_AIRY1830_MINORAXIS,
                  Constants.NATIONALGRID_N0,
                  Constants.NATIONALGRID_E0,
                  Constants.NATIONALGRID_F0,
                  Constants.NATIONALGRID_LAT0,
                  Constants.NATIONALGRID_LON0);
          double[] latlonWGS84 = OSGB36.toWGS84(latlonOSGB38[0], latlonOSGB38[1]);
          String coordinates = String.format(  "{\"type\": \"Point\", \"coordinates\": [%f,%f]}", latlonWGS84[1], latlonWGS84[0]);

          content.getAnnotations()
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_COORDINATE)
              .withBounds(new SpanBounds(m.start(), m.end()))
              .withProperty(PropertyKeys.PROPERTY_KEY_COORDINATETYPE, "osgb")
              .withProperty(PropertyKeys.PROPERTY_KEY_GEOJSON, coordinates)
              .withProperty(PropertyKeys.PROPERTY_KEY_LATITUDE, latlonWGS84[0])
              .withProperty(PropertyKeys.PROPERTY_KEY_LONGITUDE, latlonWGS84[1])
              .save();

        } catch (Exception e) {
          log().debug("Unable to convert OSGB {}", m.group(), e);
        }
      }
    }
  }
}