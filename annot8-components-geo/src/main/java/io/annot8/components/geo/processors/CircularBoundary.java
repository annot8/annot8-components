/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
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
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentName("Circular Boundary")
@ComponentDescription(
    "Extract circular geo-reference described as a distance radius from a lat,lon")
@SettingsClass(NoSettings.class)
public class CircularBoundary
    extends AbstractProcessorDescriptor<CircularBoundary.Processor, NoSettings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_DISTANCE, SpanBounds.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_LOCATION, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  public Processor createComponent(Context context, NoSettings settings) {
    return new CircularBoundary.Processor();
  }

  public static class Processor extends AbstractTextProcessor {

    public static final double EARTH_RADIUS = 6378137; // https://en.wikipedia.org/wiki/Earth_radius
    // equatorial radius

    public static final double POINTS_IN_CIRCLE = 32;

    @Override
    protected void process(Text content) {
      content
          .getAnnotations()
          .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .filter(a -> content.getText(a).filter(s -> s.contains("radius")).isPresent())
          .forEach(
              s -> {
                SpanBounds sentenceBounds = s.getBounds(SpanBounds.class).get();
                List<Annotation> inSentence =
                    content
                        .getAnnotations()
                        .getByBounds(SpanBounds.class)
                        .filter(
                            a ->
                                a.getBounds(SpanBounds.class)
                                    .filter(sentenceBounds::isWithin)
                                    .isPresent())
                        .collect(Collectors.toList());

                Optional<Annotation> coordinates =
                    inSentence.stream()
                        .filter(a -> a.getType().equals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE))
                        .findFirst();

                Optional<Annotation> distances =
                    inSentence.stream()
                        .filter(a -> a.getType().equals(AnnotationTypes.ANNOTATION_TYPE_DISTANCE))
                        .findFirst();

                if (coordinates.isEmpty() || distances.isEmpty()) {
                  return;
                }

                Annotation coordinate = coordinates.get();
                Annotation distance = distances.get();

                int begin =
                    Stream.of(coordinate, distance)
                        .flatMap(a -> a.getBounds(SpanBounds.class).stream())
                        .mapToInt(SpanBounds::getBegin)
                        .min()
                        .orElseGet(sentenceBounds::getBegin);
                int end =
                    Stream.of(coordinate, distance)
                        .flatMap(a -> a.getBounds(SpanBounds.class).stream())
                        .mapToInt(SpanBounds::getEnd)
                        .max()
                        .orElseGet(sentenceBounds::getEnd);

                Optional<Double> lon =
                    coordinate
                        .getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Double.class);
                Optional<Double> lat =
                    coordinate
                        .getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_LATITUDE, Double.class);
                Optional<Double> meters =
                    distance.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE, Double.class);

                log().trace("Found circular boundary at {},{} of size {}", lon, lat, meters);

                content
                    .getAnnotations()
                    .create()
                    .withBounds(new SpanBounds(begin, end))
                    .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
                    .withProperty(PropertyKeys.PROPERTY_KEY_COORDINATETYPE, "Circular")
                    .withPropertyIfPresent(PropertyKeys.PROPERTY_KEY_LONGITUDE, lon)
                    .withPropertyIfPresent(PropertyKeys.PROPERTY_KEY_LATITUDE, lat)
                    .withPropertyIfPresent(
                        PropertyKeys.PROPERTY_KEY_UNIT,
                        distance.getProperties().get(PropertyKeys.PROPERTY_KEY_UNIT))
                    .withPropertyIfPresent(PropertyKeys.PROPERTY_KEY_VALUE, meters)
                    .withPropertyIfPresent(
                        PropertyKeys.PROPERTY_KEY_GEOJSON, toGeoJSON(lon, lat, meters))
                    .save();
              });
    }

    private Optional<FeatureCollection> toGeoJSON(
        Optional<Double> lon, Optional<Double> lat, Optional<Double> meters) {
      if (!lon.isPresent() || !lat.isPresent() || !meters.isPresent()) {
        return Optional.empty();
      }

      Polygon polygon = circleToPolygon(lon.get(), lat.get(), meters.get());
      Feature feature = Feature.fromGeometry(polygon);
      FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
      return Optional.of(featureCollection);
    }

    private Polygon circleToPolygon(double lat, double lon, double radius) {
      var latR = Math.toRadians(lat);
      var lonR = Math.toRadians(lon);
      List<Point> coordinates = new ArrayList<>();
      for (int i = 0; i < POINTS_IN_CIRCLE; ++i) {
        coordinates.add(offset(latR, lonR, radius, (2 * Math.PI * -i) / POINTS_IN_CIRCLE));
      }
      coordinates.add(coordinates.get(0));

      return Polygon.fromLngLats(List.of(coordinates));
    }

    private Point offset(double latR, double lonR, double distance, double angle) {
      var dByR = distance / EARTH_RADIUS;
      var lat =
          Math.asin(
              Math.sin(latR) * Math.cos(dByR) + Math.cos(latR) * Math.sin(dByR) * Math.cos(angle));
      var lon =
          lonR
              + Math.atan2(
                  Math.sin(angle) * Math.sin(dByR) * Math.cos(latR),
                  Math.cos(dByR) - Math.sin(latR) * Math.sin(lat));
      return Point.fromLngLat(Math.toDegrees(lon), Math.toDegrees(lat));
    }
  }
}
