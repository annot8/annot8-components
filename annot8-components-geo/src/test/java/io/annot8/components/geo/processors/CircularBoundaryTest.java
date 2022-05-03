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
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class CircularBoundaryTest {

  private static final String TEST_TEXT =
      "No location in this line.\nArea within a 75km radius of airport, 71.695000000, 128.900000000. Other text.\n";

  private static final String TEST_SEPARATE_TEXT =
      "No location 75km in this line.\nShould not match radius of airport as in different sentences, 71.695000000, 128.900000000. Other text.\n";

  @Test
  public void testProcessor() throws Annot8Exception {

    try (Processor p = new CircularBoundary.Processor();
        LatLon.Processor latlon = new LatLon.Processor(false, 2)) {
      Item item = new TestItem();

      Text content = item.createContent(TestStringContent.class).withData(TEST_TEXT).save();

      // Add the sentence and distance annotations directly as Sentences hard to
      // instantiate
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(0, 25))
          .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .save();
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(26, 93))
          .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .save();
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(93, TEST_TEXT.length()))
          .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .save();
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(41, 45))
          .withType(AnnotationTypes.ANNOTATION_TYPE_DISTANCE)
          .withProperty(PropertyKeys.PROPERTY_KEY_UNIT, "m")
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, 75000.0)
          .save();
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
          "Circular",
          properties.get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE, String.class).orElseThrow());
      assertEquals(
          71.695000000,
          properties.get(PropertyKeys.PROPERTY_KEY_LATITUDE, Double.class).orElseThrow());
      assertEquals(
          128.900000000,
          properties.get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Double.class).orElseThrow());
      assertEquals("m", properties.get(PropertyKeys.PROPERTY_KEY_UNIT, String.class).orElseThrow());
      assertEquals(
          75000.0, properties.get(PropertyKeys.PROPERTY_KEY_VALUE, Double.class).orElseThrow());
      assertEquals(
          "{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[71.695,50.4262635],[71.9013698,50.4390255],[72.1001352,50.4768413],[72.2839368,50.5383154],[72.4459025,50.6211723],[72.5798826,50.7223314],[72.6806735,50.8380105],[72.7442243,50.9638548],[72.7678159,51.0950911],[72.7502062,51.226701],[72.6917274,51.3536091],[72.5943264,51.4708804],[72.4615381,51.573918],[72.2983838,51.6586535],[72.1111934,51.7217204],[71.9073548,51.7606007],[71.695,51.7737365],[71.4826452,51.7606007],[71.2788066,51.7217204],[71.0916162,51.6586535],[70.9284619,51.573918],[70.7956736,51.4708804],[70.6982726,51.3536091],[70.6397938,51.226701],[70.6221841,51.0950911],[70.6457757,50.9638548],[70.7093265,50.8380105],[70.8101174,50.7223314],[70.9440975,50.6211723],[71.1060632,50.5383154],[71.2898648,50.4768413],[71.4886302,50.4390255],[71.695,50.4262635]]]},\"properties\":{}}]}",
          properties
              .get(PropertyKeys.PROPERTY_KEY_GEOJSON, FeatureCollection.class)
              .map(FeatureCollection::toJson)
              .orElseThrow());
    }
  }

  @Test
  public void testSentenceFilterProcessor() throws Annot8Exception {

    try (Processor p = new CircularBoundary.Processor();
        LatLon.Processor latlon = new LatLon.Processor(false, 2)) {
      Item item = new TestItem();

      Text content =
          item.createContent(TestStringContent.class).withData(TEST_SEPARATE_TEXT).save();

      // Add the sentence annotations directly as Sentences hard to instantiate
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(0, 30))
          .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .save();
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(31, 121))
          .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .save();
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(121, TEST_SEPARATE_TEXT.length()))
          .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .save();
      content
          .getAnnotations()
          .create()
          .withBounds(new SpanBounds(12, 16))
          .withType(AnnotationTypes.ANNOTATION_TYPE_DISTANCE)
          .withProperty(PropertyKeys.PROPERTY_KEY_UNIT, "m")
          .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, 75000)
          .save();
      // Add the lat,lon annotations
      latlon.process(item);

      p.process(item);

      AnnotationStore store = content.getAnnotations();

      List<Annotation> locations =
          store.getByType(AnnotationTypes.ANNOTATION_TYPE_LOCATION).collect(Collectors.toList());

      assertEquals(0, locations.size());
    }
  }
}
