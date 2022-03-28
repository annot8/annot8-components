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
import io.annot8.api.properties.ImmutableProperties;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ComponentName("GeoBoundary")
@ComponentDescription("Extract geo location boundaries from annotated lat,lon coordinates")
@SettingsClass(NoSettings.class)
public class GeoBoundary extends AbstractProcessorDescriptor<GeoBoundary.Processor, NoSettings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, SpanBounds.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_LOCATION, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  public Processor createComponent(Context context, NoSettings settings) {
    return new GeoBoundary.Processor();
  }

  public static class Processor extends AbstractTextProcessor {

    private final Pattern boundarySeparator = Pattern.compile("\\s?;\\s?");

    @Override
    protected void process(Text content) {
      List<Annotation> coordinates =
          content
              .getAnnotations()
              .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_COORDINATE)
              .sorted(Comparator.comparing(a -> a.getBounds(SpanBounds.class).get().getBegin()))
              .collect(Collectors.toList());

      if (coordinates.size() < 2) return;

      List<List<Annotation>> groups = new ArrayList<>();
      List<Annotation> stack = new ArrayList<>();

      Annotation aCurr = coordinates.get(0);
      for (int i = 1; i < coordinates.size(); i++) {
        stack.add(aCurr);
        Annotation a2 = coordinates.get(i);

        SpanBounds s1 = aCurr.getBounds(SpanBounds.class).get();
        SpanBounds s2 = a2.getBounds(SpanBounds.class).get();

        boolean addToLocation = false;

        if (!s2.isOverlaps(s1)) {
          Optional<String> contentBetween =
              new SpanBounds(s1.getEnd(), s2.getBegin()).getData(content);
          addToLocation =
              contentBetween.filter(o -> boundarySeparator.matcher(o).find()).isPresent();
        }

        if (!addToLocation) {
          if (stack.size() > 1) {
            groups.add(new ArrayList<>(stack));
          }
          stack.clear();
        }
        aCurr = a2;
      }
      stack.add(aCurr);
      if (stack.size() > 1) {
        groups.add(new ArrayList<>(stack));
        stack.clear();
      }

      groups.forEach(
          group -> {
            int begin = group.get(0).getBounds(SpanBounds.class).get().getBegin();
            int end = group.get(group.size() - 1).getBounds(SpanBounds.class).get().getEnd();

            SpanBounds bounds = new SpanBounds(begin, end);
            content
                .getAnnotations()
                .create()
                .withBounds(bounds)
                .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
                .withProperty(PropertyKeys.PROPERTY_KEY_COORDINATETYPE, "GeoLoction")
                .withProperty(
                    PropertyKeys.PROPERTY_KEY_VALUE,
                    bounds.getData(content).orElse("").replaceAll("\\s+", " "))
                .withPropertyIfPresent(PropertyKeys.PROPERTY_KEY_GEOJSON, getGeoJson(group))
                .save();

            content.getAnnotations().delete(group);
          });
    }

    private Optional<FeatureCollection> getGeoJson(List<Annotation> coordinates) {

      try {
        List<Point> points =
            coordinates.stream().map(this::toCoordinateJson).collect(Collectors.toList());
        if (points.size() < 3) {
          return Optional.empty();
        }
        // Should start and end with the same point
        if (!points.get(0).equals(points.get(points.size() - 1))) {
          points.add(points.get(0));
        }
        Polygon polygon = Polygon.fromLngLats(List.of(points));
        Feature feature = Feature.fromGeometry(polygon);
        FeatureCollection featureCollection = FeatureCollection.fromFeature(feature);
        return Optional.of(featureCollection);
      } catch (Exception e) {
        return Optional.empty();
      }
    }

    private Point toCoordinateJson(Annotation a) {
      ImmutableProperties properties = a.getProperties();
      double lon = properties.get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Double.class).orElseThrow();
      double lat = properties.get(PropertyKeys.PROPERTY_KEY_LATITUDE, Double.class).orElseThrow();
      return Point.fromLngLat(lon, lat);
    }
  }
}
