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
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.gazetteers.processors.AhoCorasick;
import io.annot8.components.gazetteers.processors.impl.MapGazetteer;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ComponentName("CountryGazetteer")
@ComponentDescription("Extract countries from text")
public class CountryGazetteer extends AhoCorasick<CountryGazetteer.Settings> {

  @Override
  protected Processor createComponent(Context context, CountryGazetteer.Settings countryGazetteerSettings) {
    return new Processor(new MapGazetteer(getCountryData(countryGazetteerSettings)), countryGazetteerSettings);
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
    countries.values().forEach(metadata -> metadata.put(PropertyKeys.PROPERTY_KEY_GEOJSON, geojsons.get(metadata.get("cca3").toString())));
  }

  private Map<Set<String>, Map<String, Object>> getCountryData(Settings settings) {
    Map<Set<String>, Map<String, Object>> output = new HashMap<>();

    Json.createReader(CountryGazetteer.class.getResourceAsStream("countries.json"))
        .readArray().forEach(jv -> {
      JsonObject jo = jv.asJsonObject();
      Set<String> names = getNames(jo);
      Map<String, Object> metadata = new HashMap<>();
      metadata.put("cca3", jo.getString("cca3"));
      if (settings.isMetadata()) {
        addJsonMetadata(jo, metadata);
      }
      output.put(names, metadata);
    });

    if (settings.isGeojson()) {
      addGeojsonsToMetadata(output);
    }
    return output;
  }

  private Map<String, JsonObject> getGeojsons() {
    Map<String, JsonObject> output = new HashMap<>();
    JsonReader reader = Json.createReader(CountryGazetteer.class.getResourceAsStream("countries.geojson"));
    reader.readObject().getJsonArray("features").forEach(
        feature -> output.put(
            feature.asJsonObject().getJsonObject("properties").getString("ISO_A3"),
            feature.asJsonObject().getJsonObject("geometry"))
    );
    return output;
  }

  private void addJsonMetadata(JsonObject jo, Map<String, Object> metadata) {
    metadata.put("area", jo.getInt("area"));
    metadata.put("borders", jo.getJsonArray("borders").stream().map(JsonValue::toString).map(this::removeQuotes).collect(Collectors.toSet()));
    metadata.put("capitals", jo.getJsonArray("capital").stream().map(JsonValue::toString).map(this::removeQuotes).collect(Collectors.toSet()));
    metadata.put("demonym", jo.getString("demonym"));
    metadata.put("flag", jo.getString("flag"));
    metadata.put("independent", jo.getBoolean("independent"));
    metadata.put("landlocked", jo.getBoolean("landlocked"));
    List<Double> latlng = jo.getJsonArray("latlng").stream().map(JsonValue::toString).map(Double::parseDouble).collect(Collectors.toList());
    metadata.put(PropertyKeys.PROPERTY_KEY_LATITUDE, latlng.get(0));
    metadata.put(PropertyKeys.PROPERTY_KEY_LONGITUDE, latlng.get(1));
    metadata.put("region", jo.getString("region"));
    metadata.put("subregion", jo.getString("subregion"));
  }

  private Set<String> getNames(JsonObject jo) {
    Set<String> initialSet = jo.getJsonObject("name").getJsonObject("native").values().stream()
        .flatMap(a -> a.asJsonObject().values().stream().map(JsonValue::toString)).collect(Collectors.toSet());
    initialSet.add(jo.getJsonObject("name").getString("common"));
    initialSet.add(jo.getJsonObject("name").getString("official"));
    jo.getJsonObject("translations").values().forEach(a -> addNamesToSet(initialSet, a.asJsonObject()));
    jo.getJsonArray("altSpellings").forEach(s -> initialSet.add(s.toString()));
    return initialSet.stream().map(this::removeQuotes).collect(Collectors.toSet());
  }

  private String removeQuotes(String input) {
    return input.replaceAll("\"", "");
  }

  private void addNamesToSet(Set<String> runningSet, JsonObject jo) {
    runningSet.add(jo.getString("common"));
    runningSet.add(jo.getString("official"));
  }

  public static class Settings extends AhoCorasick.Settings {
    private boolean geojson;
    private boolean metadata;

    @JsonbCreator
    public Settings(@JsonbProperty("geojson") boolean geojson, @JsonbProperty("metadata") boolean metadata){
      this.geojson = geojson;
      this.metadata = metadata;
      setAdditionalData(true);
      setCaseSensitive(true);
      setExactWhitespace(false);
      setPlurals(false);
      setType(AnnotationTypes.ANNOTATION_TYPE_LOCATION);
    }

    public boolean isGeojson(){
      return geojson;
    }

    public boolean isMetadata() {
      return metadata;
    }

    @Override
    public boolean validate() {
      return true;
    }
  }

}
