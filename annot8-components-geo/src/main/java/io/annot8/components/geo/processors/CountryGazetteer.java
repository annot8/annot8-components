/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.gazetteers.processors.AhoCorasick;
import io.annot8.components.gazetteers.processors.impl.MapGazetteer;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

@ComponentName("Country Gazetteer")
@ComponentDescription("Extract countries from text")
@SettingsClass(CountryGazetteer.Settings.class)
public class CountryGazetteer
    extends AbstractProcessorDescriptor<AhoCorasick.Processor, CountryGazetteer.Settings> {

  @Override
  protected AhoCorasick.Processor createComponent(
      Context context, CountryGazetteer.Settings settings) {
    AhoCorasick.Settings s = new AhoCorasick.Settings();
    s.setAdditionalData(true);
    s.setCaseSensitive(settings.isCaseSensitive());
    s.setExactWhitespace(false);
    s.setPlurals(false);
    s.setSubType(settings.getSubType());
    s.setType(AnnotationTypes.ANNOTATION_TYPE_LOCATION);

    return new AhoCorasick.Processor(new MapGazetteer(getCountryData(settings)), s);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_LOCATION, SpanBounds.class)
        .build();
  }

  private void addGeojsonsToMetadata(Map<Set<String>, Map<String, Object>> countries) {
    Map<String, JsonObject> geojsons = getGeojsons();
    countries
        .values()
        .forEach(
            metadata ->
                metadata.put(
                    PropertyKeys.PROPERTY_KEY_GEOJSON,
                    geojsons.get(metadata.get("cca3").toString())));
  }

  private Map<Set<String>, Map<String, Object>> getCountryData(Settings settings) {
    Map<Set<String>, Map<String, Object>> output = new HashMap<>();

    Json.createReader(CountryGazetteer.class.getResourceAsStream("countries.json"))
        .readArray()
        .forEach(
            jv -> {
              JsonObject jo = jv.asJsonObject();
              Set<String> names = getNames(jo, settings.isIncludeCountryCodes());
              Map<String, Object> metadata = new HashMap<>();
              metadata.put("cca3", jo.getString("cca3"));
              if (settings.isMetadata()) {
                addJsonMetadata(jo, metadata);
              }
              output.put(names, metadata);
            });

    if (settings.isGeoJson()) {
      addGeojsonsToMetadata(output);
    }
    return output;
  }

  private Map<String, JsonObject> getGeojsons() {
    Map<String, JsonObject> output = new HashMap<>();
    JsonReader reader =
        Json.createReader(CountryGazetteer.class.getResourceAsStream("countries.geojson"));
    reader
        .readObject()
        .getJsonArray("features")
        .forEach(
            feature ->
                output.put(
                    feature.asJsonObject().getJsonObject("properties").getString("ISO_A3"),
                    feature.asJsonObject().getJsonObject("geometry")));
    return output;
  }

  private void addJsonMetadata(JsonObject jo, Map<String, Object> metadata) {
    metadata.put("area", jo.getInt("area"));
    metadata.put(
        "borders",
        jo.getJsonArray("borders").stream()
            .map(JsonValue::toString)
            .map(this::removeQuotes)
            .collect(Collectors.toSet()));
    metadata.put(
        "capitals",
        jo.getJsonArray("capital").stream()
            .map(JsonValue::toString)
            .map(this::removeQuotes)
            .collect(Collectors.toSet()));

    try {
      metadata.put("demonym", jo.getJsonObject("demonyms").getJsonObject("eng").getString("m"));
    } catch (NullPointerException npe) {
      // Demonym can't be found, so skip
    }

    metadata.put("flag", jo.getString("flag"));

    try {
      metadata.put("independent", jo.getBoolean("independent"));
    } catch (ClassCastException cce) {
      // Kosovo has a null value for independent in the dataset we have, so need to catch and skip
      // this
    }

    metadata.put("landlocked", jo.getBoolean("landlocked"));
    List<Double> latlng =
        jo.getJsonArray("latlng").stream()
            .map(JsonValue::toString)
            .map(Double::parseDouble)
            .collect(Collectors.toList());
    metadata.put(PropertyKeys.PROPERTY_KEY_LATITUDE, latlng.get(0));
    metadata.put(PropertyKeys.PROPERTY_KEY_LONGITUDE, latlng.get(1));
    metadata.put("region", jo.getString("region"));
    metadata.put("subregion", jo.getString("subregion"));
  }

  private Set<String> getNames(JsonObject jo, boolean includeCountryCodes) {
    Set<String> initialSet =
        jo.getJsonObject("name").getJsonObject("native").values().stream()
            .flatMap(a -> a.asJsonObject().values().stream().map(JsonValue::toString))
            .collect(Collectors.toSet());
    initialSet.add(jo.getJsonObject("name").getString("common"));
    initialSet.add(jo.getJsonObject("name").getString("official"));
    jo.getJsonObject("translations")
        .values()
        .forEach(a -> addNamesToSet(initialSet, a.asJsonObject()));
    jo.getJsonArray("altSpellings").forEach(s -> initialSet.add(s.toString()));
    return initialSet.stream()
        .map(this::removeQuotes)
        .filter(s -> includeCountryCodes || s.length() > 2)
        .collect(Collectors.toSet());
  }

  private String removeQuotes(String input) {
    return input.replaceAll("\"", "");
  }

  private void addNamesToSet(Set<String> runningSet, JsonObject jo) {
    runningSet.add(jo.getString("common"));
    runningSet.add(jo.getString("official"));
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private boolean caseSensitive = true;
    private boolean geoJson = true;
    private boolean metadata = false;
    private boolean includeCountryCodes = false;
    private String subType;

    public Settings() {
      // Default constructor
    }

    public Settings(boolean geoJson, boolean metadata) {
      this.geoJson = geoJson;
      this.metadata = metadata;
    }

    public Settings(
        boolean geoJson,
        boolean metadata,
        boolean caseSensitive,
        String subType,
        boolean includeCountryCodes) {
      this.geoJson = geoJson;
      this.metadata = metadata;
      this.caseSensitive = caseSensitive;
      this.subType = subType;
      this.includeCountryCodes = includeCountryCodes;
    }

    @Description(
        value = "Only annotate matches with the same case as the data file",
        defaultValue = "true")
    public boolean isCaseSensitive() {
      return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
      this.caseSensitive = caseSensitive;
    }

    @Description(value = "Add GeoJSON information to annotations", defaultValue = "true")
    public boolean isGeoJson() {
      return geoJson;
    }

    public void setGeoJson(boolean geoJson) {
      this.geoJson = geoJson;
    }

    @Description(value = "Add country metadata to annotations", defaultValue = "false")
    public boolean isMetadata() {
      return metadata;
    }

    public void setMetadata(boolean metadata) {
      this.metadata = metadata;
    }

    @Description("Sub-type to assign to annotations, or null")
    public String getSubType() {
      return subType;
    }

    public void setSubType(String subType) {
      this.subType = subType;
    }

    @Description(
        value = "Include two letter country codes in list of country names",
        defaultValue = "false")
    public boolean isIncludeCountryCodes() {
      return includeCountryCodes;
    }

    public void setIncludeCountryCodes(boolean includeCountryCodes) {
      this.includeCountryCodes = includeCountryCodes;
    }

    @Override
    public boolean validate() {
      return true;
    }
  }
}
