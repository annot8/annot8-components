/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.components.geo.processors.geonames.GeoNamesAdditionalProperties;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class EnrichWithGeoNamesTest {
  @Test
  public void test() throws URISyntaxException {
    Item i = new TestItem();
    Text t =
        i.createContent(Text.class)
            .withData(
                "Last year, I visited Mead's Bay, a nice small harbour and The Captain's Club")
            .save();

    t.getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
        .withBounds(new SpanBounds(21, 31))
        .save();

    t.getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
        .withBounds(new SpanBounds(33, 53))
        .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, "Little Harbour")
        .save();

    t.getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
        .withBounds(new SpanBounds(58, 76))
        .save();

    EnrichWithGeoNames.Settings s = new EnrichWithGeoNames.Settings();
    s.setGeoJson(true);
    s.setProperties(GeoNamesAdditionalProperties.ALL);
    s.setGeonamesFile(
        Paths.get(GeoNamesGazetteerTest.class.getResource("AI.txt").toURI()).toFile());

    try (EnrichWithGeoNames.Processor p = new EnrichWithGeoNames.Processor(s)) {

      ProcessorResponse pr = p.process(i);
      assertEquals(ProcessorResponse.ok(), pr);

      List<Annotation> annotations =
          t.getAnnotations()
              .getAll()
              .sorted(SortUtils.SORT_BY_SPANBOUNDS)
              .collect(Collectors.toList());
      assertEquals(3, annotations.size());

      Annotation a1 = annotations.get(0);
      assertTrue(a1.getProperties().getAll().size() > 0);
      assertTrue(a1.getProperties().has(PropertyKeys.PROPERTY_KEY_GEOJSON));

      Annotation a2 = annotations.get(1);
      assertTrue(a2.getProperties().getAll().size() > 1); // This annotation already had a
      // VALUE
      assertTrue(a2.getProperties().has(PropertyKeys.PROPERTY_KEY_GEOJSON));

      Annotation a3 = annotations.get(2);
      assertTrue(a3.getProperties().getAll().isEmpty());
    }
  }
}
