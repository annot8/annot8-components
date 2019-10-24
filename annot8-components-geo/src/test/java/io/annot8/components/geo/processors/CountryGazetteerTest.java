/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

import org.junit.jupiter.api.Test;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.properties.ImmutableProperties;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.components.gazetteers.processors.AhoCorasick;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;

public class CountryGazetteerTest {
  @Test
  public void testCreation() {
    CountryGazetteer countryGazetteer = new CountryGazetteer();
    CountryGazetteer.Processor countryProcessor =
        countryGazetteer.createComponent(null, new CountryGazetteer.Settings(true, true));
    assertNotNull(countryProcessor);
    countryProcessor.close();
  }

  @Test
  public void testSimple() {
    CountryGazetteer countryGazetteer = new CountryGazetteer();
    AhoCorasick.Processor countryProcessor =
        countryGazetteer.createComponent(null, new CountryGazetteer.Settings(true, true));
    TestItem testItem = new TestItem();
    ImmutableProperties properties =
        assertGetsCountryAndReturnAnnotation(countryProcessor, testItem, "Jamaica").getProperties();
    assertEquals("JAM", properties.getOrDefault("cca3", ""));
  }

  @Test
  public void testDjibouti() {
    CountryGazetteer countryGazetteer = new CountryGazetteer();
    AhoCorasick.Processor countryProcessor =
        countryGazetteer.createComponent(null, new CountryGazetteer.Settings(true, true));
    TestItem testItem = new TestItem();
    ImmutableProperties properties =
        assertGetsCountryAndReturnAnnotation(
                countryProcessor,
                testItem,
                "\u062c\u0645\u0647\u0648\u0631\u064a\u0629 \u062c\u064a\u0628\u0648\u062a\u064a")
            .getProperties();
    assertEquals("DJI", properties.getOrDefault("cca3", ""));
    assertEquals(Set.of("SOM", "ETH", "ERI"), properties.get("borders").orElseThrow());
    assertEquals(23200, properties.getOrDefault("area", 0));
    assertEquals("Djibouti", properties.getOrDefault("demonym", ""));
    assertEquals(true, properties.getOrDefault("independent", false));
    assertEquals(false, properties.getOrDefault("landlocked", true));
    assertEquals(Set.of("Djibouti"), properties.get("capitals").orElseThrow());
    assertEquals("Eastern Africa", properties.getOrDefault("subregion", ""));
    assertEquals("Africa", properties.getOrDefault("region", ""));
    assertEquals(11.5, properties.getOrDefault(PropertyKeys.PROPERTY_KEY_LATITUDE, 0.0));
    assertEquals(43.0, properties.getOrDefault(PropertyKeys.PROPERTY_KEY_LONGITUDE, 0.0));
  }

  @Test
  public void testCaseSensitive() {
    CountryGazetteer countryGazetteer = new CountryGazetteer();
    CountryGazetteer.Settings countryGazetteerSettings = new CountryGazetteer.Settings(true, true);
    countryGazetteerSettings.setCaseSensitive(true);
    AhoCorasick.Processor countryProcessor =
        countryGazetteer.createComponent(null, countryGazetteerSettings);
    TestItem testItem = new TestItem();
    TestStringContent content =
        testItem
            .createContent(TestStringContent.class)
            .withData("Last month, Peter visited the coast of JAMaica")
            .save();
    countryProcessor.process(testItem);
    AnnotationStore annotationStore = content.getAnnotations();
    assertEquals(0, annotationStore.getAll().count());
  }

  private Annotation assertGetsCountryAndReturnAnnotation(
      AhoCorasick.Processor countryProcessor, TestItem testItem, String country) {
    TestStringContent content =
        testItem
            .createContent(TestStringContent.class)
            .withData("Last month, Peter visited the coast of " + country)
            .save();

    countryProcessor.process(testItem);
    Annotation annotation = content.getAnnotations().getAll().findFirst().orElseThrow();
    assertEquals(country, annotation.getBounds().getData(content).orElseThrow());
    return annotation;
  }
}
