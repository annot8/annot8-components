/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.components.geo.processors.geonames.GeoNamesAdditionalProperties;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class GeoNamesGazetteerTest {
  @Test
  public void test() throws URISyntaxException {
    Item item = new TestItem();
    Text content =
        item.createContent(TestStringContent.class)
            .withData("Visiting Shoal Bay is a lovely thing to do on a sunny afternoon.")
            .save();

    GeoNamesGazetteer.Settings s = new GeoNamesGazetteer.Settings();
    s.setAdditionalProperties(GeoNamesAdditionalProperties.ALL);
    s.setGeoJson(true);
    s.setGeonamesFile(
        Paths.get(GeoNamesGazetteerTest.class.getResource("AI.txt").toURI()).toFile());
    assertTrue(s.validate());

    GeoNamesGazetteer gng = new GeoNamesGazetteer();
    assertNotNull(gng.capabilities());

    Processor p = gng.createComponent(null, s);

    p.process(item);

    assertEquals(1L, content.getAnnotations().getAll().count());

    Annotation a = content.getAnnotations().getAll().findFirst().get();
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_LOCATION, a.getType());
    assertEquals("Shoal Bay", a.getBounds().getData(content).get());

    assertEquals("AI", a.getProperties().get(PropertyKeys.PROPERTY_KEY_COUNTRY).get());
    assertNotNull(a.getProperties().get(PropertyKeys.PROPERTY_KEY_LATITUDE).get());
    assertNotNull(a.getProperties().get(PropertyKeys.PROPERTY_KEY_LONGITUDE).get());
    assertNotNull(a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());

    p.close();
  }
}
