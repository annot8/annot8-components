/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mapbox.geojson.FeatureCollection;
import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.properties.Properties;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class GeoBoundaryTest {

  private static final String TEST_TEXT =
      "No location in this line.\nBounded by the following coordinates: 52.052907, 39.533422; 52.025890, 38.822057; 51.576055, 38.481481. Other text.\n";

  @Test
  public void testProcessor() throws Annot8Exception {

    try (Processor p = new GeoBoundary.Processor();
        LatLon.Processor latlon = new LatLon.Processor(false, 2)) {
      Item item = new TestItem();

      Text content = item.createContent(TestStringContent.class).withData(TEST_TEXT).save();

      // Add the lat,lon annotations
      latlon.process(item);

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> locations =
          store.getByType(AnnotationTypes.ANNOTATION_TYPE_LOCATION).collect(Collectors.toList());

      assertEquals(1, locations.size());

      Annotation location = locations.get(0);
      Properties properties = location.getProperties();
      assertEquals(
          "GeoLoction",
          properties.get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE, String.class).orElseThrow());
      assertEquals(
          "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[39.533422,52.052907],[38.822057,52.02589],[38.481481,51.576055],[39.533422,52.052907]]]},\"properties\":{}}]}",
          properties
              .get(PropertyKeys.PROPERTY_KEY_GEOJSON, FeatureCollection.class)
              .map(FeatureCollection::toJson)
              .orElseThrow());
    }
  }
}
