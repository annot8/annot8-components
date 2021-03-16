/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.components.geo.processors.geonames.GeoNamesAdditionalProperties;
import io.annot8.components.geo.processors.geonames.GeoNamesUtils;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@ComponentName("Enrich with GeoNames")
@ComponentDescription(
    "Add information to previously extracted Locations from the GeoNames database")
@SettingsClass(EnrichWithGeoNames.Settings.class)
public class EnrichWithGeoNames
    extends AbstractProcessorDescriptor<EnrichWithGeoNames.Processor, EnrichWithGeoNames.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_LOCATION, SpanBounds.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractProcessor {
    private final Map<String, Integer> geonameEntryToId = new HashMap<>();
    private final Map<Integer, Map<String, Object>> idToProperties = new HashMap<>();

    public Processor(Settings settings) {
      try {
        AtomicInteger l = new AtomicInteger(0);
        GeoNamesUtils.loadGazetteer(
                settings.getGeonamesFile(), settings.getProperties(), settings.isGeoJson(), 0)
            .forEach(
                (set, properties) -> {
                  int id = l.getAndIncrement();

                  set.forEach(
                      s ->
                          geonameEntryToId.putIfAbsent(
                              s, id)); // Take the first entry if a name appears more than once
                  if (!properties.isEmpty()) idToProperties.put(id, properties);
                });

      } catch (IOException e) {
        throw new BadConfigurationException("Unable to load GeoNames from configuration", e);
      }
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents()
          .forEach(
              c ->
                  c.getAnnotations()
                      .getByBoundsAndType(
                          SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_LOCATION)
                      .forEach(
                          a -> {
                            Set<String> values = new HashSet<>();

                            a.getProperties()
                                .get(PropertyKeys.PROPERTY_KEY_VALUE, String.class)
                                .ifPresent(values::add);
                            a.getBounds(SpanBounds.class)
                                .flatMap(sb -> sb.getData(c, String.class))
                                .ifPresent(values::add);

                            values.stream()
                                .filter(geonameEntryToId::containsKey)
                                .findFirst()
                                .ifPresent(
                                    s -> {
                                      Map<String, Object> props =
                                          idToProperties.getOrDefault(
                                              geonameEntryToId.get(s), Collections.emptyMap());

                                      Annotation.Builder b = c.getAnnotations().edit(a);
                                      props.forEach(b::withProperty);
                                      b.save();
                                    });
                          }));
      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private File geonamesFile = null;
    private GeoNamesAdditionalProperties properties = GeoNamesAdditionalProperties.BASIC;
    private boolean geoJson = true;

    @Override
    public boolean validate() {
      return properties != null && geonamesFile != null;
    }

    @Description("Location of the GeoNames data file")
    public File getGeonamesFile() {
      return geonamesFile;
    }

    public void setGeonamesFile(File geonamesFile) {
      this.geonamesFile = geonamesFile;
    }

    @Description(
        value = "Which fields from the GeoNames data should be added to annotations",
        defaultValue = "BASIC")
    public GeoNamesAdditionalProperties getProperties() {
      return properties;
    }

    public void setProperties(GeoNamesAdditionalProperties properties) {
      this.properties = properties;
    }

    @Description(value = "Add GeoJSON to the annotation", defaultValue = "true")
    public boolean isGeoJson() {
      return geoJson;
    }

    public void setGeoJson(boolean geoJson) {
      this.geoJson = geoJson;
    }
  }
}
