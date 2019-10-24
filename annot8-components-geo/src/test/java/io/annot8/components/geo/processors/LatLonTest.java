/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class LatLonTest {

  @Test
  public void testLatlon() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "London is located in the UK, at 51.507, -0.125. Edinburgh is also in the UK, at 55.953,-3.188.",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(2, annotations.size());

    assertLondon(annotations.get(32));
    assertEdinburgh(annotations.get(80));
  }

  @Test
  public void testLonLat() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "London is located in the UK, at -0.125, 51.507. Edinburgh is also in the UK, at -3.188,55.953.",
            true,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(2, annotations.size());

    assertLondon(annotations.get(32));
    assertEdinburgh(annotations.get(80));
  }

  @Test
  public void testOutOfRange() {
    assertEquals(
        0,
        getAnnotations(
                "The following coordinates aren't valid: 987.654,32.1; 12.3,456.789.", false, 0)
            .count());
  }

  @Test
  public void testNoDelimiter() {
    assertEquals(
        0,
        getAnnotations("The following coordinates aren't valid: 87.65432.1; 12.3-56.789.", false, 0)
            .count());
  }

  @Test
  public void testMinDP() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "London is located in the UK, at 51.507, -0.125. Edinburgh is also in the UK, at -3.2,56.0.",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(1, annotations.size());

    assertLondon(annotations.get(32));
  }

  @Test
  public void testDegreeSym() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "London is located in the UK, at 51.507°, -0.125°. Edinburgh is also in the UK, at 55.9530,-3.1880.\n"
                + "But darkest Peru is at -9.19°, -75.0152° and South Korea at 35.9078°, 127.7669°.",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(4, annotations.size());

    assertLondon(annotations.get(32));
    assertEdinburgh(annotations.get(82));
    assertPeru(annotations.get(122));
    assertSouthKorea(annotations.get(159));
  }

  @Test
  public void testCardinalSym() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "London is located in the UK, at 51.507° N, 0.125°W. Edinburgh is also in the UK, at 55.9530°N,3.1880° W."
                + "But darkest Peru is at 9.19° S, 75.0152°W and South Korea at 35.9078°N, 127.7669° E.",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(4, annotations.size());

    assertLondon(annotations.get(32));
    assertEdinburgh(annotations.get(84));
    assertPeru(annotations.get(127));
    assertSouthKorea(annotations.get(165));
  }

  @Test
  public void testMoney() {
    assertEquals(
        0,
        getAnnotations(
                "It may cost £2,000 a month to live in London, but that doesn't mean there are any coordiantes in this sentence!",
                false,
                0)
            .count());
  }

  @Test
  public void testSymbols() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "London is located in the UK, at 51°30'26\"N 0°7'39\"W. The following coordinates aren't valid: 12°34'56\"N; 12\"34'N 12\"34'S.",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(1, annotations.size());

    assertLondonDms(annotations.get(32));
  }

  @Test
  public void testSpaces() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "London is located in the UK, at (51 30 26 N, 0 7 39 W). The following coordinates aren't valid: 12°34'56\"N; 12°34'N 12°34'S.",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(1, annotations.size());

    assertLondonDms(annotations.get(33));
  }

  @Test
  public void testPunctuation() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations("London is located in the UK, at 51-30,26 N, 000-07,39 W.", false, 2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(1, annotations.size());

    assertLondonDms(annotations.get(32));
  }

  @Test
  public void testDegMin() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "Warsaw is in Poland, at (5214N 02101E). Rio de Janeiro is in Brazil, at (2254S 04312W). The following coordinates aren't valid: (9999N 01234E); (9000S 36000W).",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(2, annotations.size());

    assertWarsaw(annotations.get(25), false);
    assertRio(annotations.get(73), false);
  }

  @Test
  public void testDegMinSec() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "Warsaw is in Poland, at (521404N 0210104E). Rio de Janeiro is in Brazil, at (225404S 0431204W). The following coordinates aren't valid: (9999N 01234E); (9000S 36000W).",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(2, annotations.size());

    assertWarsaw(annotations.get(25), true);
    assertRio(annotations.get(77), true);
  }

  @Test
  public void testDegMinSecSlash() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "Warsaw is in Poland, at (521404N/0210104E). Rio de Janeiro is in Brazil, at (225404S/0431204W). The following coordinates aren't valid: (9999N/01234E); (9000S/36000W).",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(2, annotations.size());

    assertWarsaw(annotations.get(25), true);
    assertRio(annotations.get(77), true);
  }

  @Test
  public void testDegMinSecText() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "Warsaw is in Poland, at Lat 52°14.0'N Lon 21°1.0'E. Rio de Janeiro is in Brazil at Latitude 22° 54.0' S, Longitude 43° 12.0' W",
            false,
            2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(2, annotations.size());

    assertWarsaw(annotations.get(24), false);
    assertRio(annotations.get(83), false);
  }

  @Test
  public void testNESW() {
    Map<Integer, Annotation> annotations = new HashMap<>();
    getAnnotations(
            "521404N 0210104E, 521404N 0210104W, 521404S 0210104E, 521404S 0210104W", false, 2)
        .forEach(a -> annotations.put(a.getBounds(SpanBounds.class).get().getBegin(), a));

    assertEquals(4, annotations.size());

    double a = 52.23444444444444;
    double b = 21.017777777777777;

    Annotation a1 = annotations.get(0);
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a1.getType());
    assertEquals("dms", a1.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals(a + ";" + b, a1.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[" + b + "," + a + "]}",
        a1.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());

    Annotation a2 = annotations.get(18);
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a2.getType());
    assertEquals("dms", a2.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals(a + ";" + -b, a2.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[" + -b + "," + a + "]}",
        a2.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());

    Annotation a3 = annotations.get(36);
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a3.getType());
    assertEquals("dms", a3.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals(-a + ";" + b, a3.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[" + b + "," + -a + "]}",
        a3.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());

    Annotation a4 = annotations.get(54);
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a4.getType());
    assertEquals("dms", a4.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals(-a + ";" + -b, a4.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[" + -b + "," + -a + "]}",
        a4.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
  }

  private Stream<Annotation> getAnnotations(String text, boolean lonLat, int minDP) {
    Item item = new TestItem();
    Text content = item.createContent(TestStringContent.class).withData(text).save();

    try (Processor p = new LatLon.Processor(lonLat, minDP)) {
      p.process(item);
    }

    return content.getAnnotations().getAll();
  }

  private void assertLondon(Annotation a) {
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a.getType());
    assertEquals("dd", a.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals("51.507;-0.125", a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[-0.125,51.507]}",
        a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
  }

  private void assertLondonDms(Annotation a) {
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a.getType());
    assertEquals("dms", a.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals(
        "51.507222222222225;-0.1275", a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[-0.1275,51.507222222222225]}",
        a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
  }

  private void assertEdinburgh(Annotation a) {
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a.getType());
    assertEquals("dd", a.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals("55.953;-3.188", a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[-3.188,55.953]}",
        a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
  }

  private void assertPeru(Annotation a) {
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a.getType());
    assertEquals("dd", a.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals("-9.19;-75.0152", a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[-75.0152,-9.19]}",
        a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
  }

  private void assertSouthKorea(Annotation a) {
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a.getType());
    assertEquals("dd", a.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals("35.9078;127.7669", a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[127.7669,35.9078]}",
        a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
  }

  private void assertWarsaw(Annotation a, boolean seconds) {
    double lat, lon;
    if (seconds) {
      lat = 52.23444444444444;
      lon = 21.017777777777777;
    } else {
      lat = 52.233333333333334;
      lon = 21.016666666666666;
    }
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a.getType());
    assertEquals("dms", a.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals(lat + ";" + lon, a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}",
        a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
  }

  private void assertRio(Annotation a, boolean seconds) {
    double lat, lon;
    if (seconds) {
      lat = -22.90111111111111;
      lon = -43.20111111111111;
    } else {
      lat = -22.9;
      lon = -43.2;
    }
    assertEquals(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, a.getType());
    assertEquals("dms", a.getProperties().get(PropertyKeys.PROPERTY_KEY_COORDINATETYPE).get());
    assertEquals(lat + ";" + lon, a.getProperties().get(PropertyKeys.PROPERTY_KEY_VALUE).get());
    assertEquals(
        "{\"type\":\"Point\",\"coordinates\":[" + lon + "," + lat + "]}",
        a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON).get());
  }
}
