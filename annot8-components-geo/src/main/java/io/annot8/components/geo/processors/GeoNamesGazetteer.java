/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.Annot8RuntimeException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.gazetteers.processors.AhoCorasick;
import io.annot8.components.gazetteers.processors.impl.MapGazetteer;
import io.annot8.components.geo.processors.geonames.GeoNamesAdditionalProperties;
import io.annot8.components.geo.processors.geonames.GeoNamesUtils;
import io.annot8.conventions.AnnotationTypes;
import java.io.File;
import java.io.IOException;

@ComponentName("GeoNames Gazetteer")
@ComponentDescription("Use downloaded GeoNames data as a gazetteer to identify locations")
@SettingsClass(GeoNamesGazetteer.Settings.class)
public class GeoNamesGazetteer
    extends AbstractProcessorDescriptor<AhoCorasick.Processor, GeoNamesGazetteer.Settings> {

  @Override
  protected AhoCorasick.Processor createComponent(Context context, Settings settings) {
    AhoCorasick.Settings s = new AhoCorasick.Settings();
    s.setAdditionalData(true);
    s.setCaseSensitive(settings.isCaseSensitive());
    s.setExactWhitespace(false);
    s.setPlurals(false);
    s.setSubType(settings.getSubType());
    s.setType(AnnotationTypes.ANNOTATION_TYPE_LOCATION);

    try {
      return new AhoCorasick.Processor(
          new MapGazetteer(
              GeoNamesUtils.loadGazetteer(
                  settings.getGeonamesFile(),
                  settings.getAdditionalProperties(),
                  settings.isGeoJson(),
                  settings.getMinimumPopulation())),
          s);
    } catch (IOException e) {
      throw new Annot8RuntimeException("Unable to read GeoNames file into gazetteer", e);
    }
  }

  @Override
  public Capabilities capabilities() {
    // Copied from AhoCorasick.capabilities()
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_LOCATION, SpanBounds.class)
        .withCreatesGroups("aliases")
        .build();
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private GeoNamesAdditionalProperties additionalProperties = GeoNamesAdditionalProperties.BASIC;
    private boolean caseSensitive = true;
    private boolean geoJson = true;
    private File geonamesFile = null;
    private String subType = null;
    private int minimumPopulation = 0;

    @Override
    public boolean validate() {
      return geonamesFile != null
          && geonamesFile.exists()
          && geonamesFile.isFile()
          && geonamesFile.canRead();
    }

    @Description(
        value = "Which fields from the GeoNames data should be added as additional properties",
        defaultValue = "BASIC")
    public GeoNamesAdditionalProperties getAdditionalProperties() {
      return additionalProperties;
    }

    public void setAdditionalProperties(GeoNamesAdditionalProperties additionalProperties) {
      this.additionalProperties = additionalProperties;
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

    @Description(value = "Add GeoJSON to the annotation", defaultValue = "true")
    public boolean isGeoJson() {
      return geoJson;
    }

    public void setGeoJson(boolean geoJson) {
      this.geoJson = geoJson;
    }

    @Description("Location of the GeoNames data file")
    public File getGeonamesFile() {
      return geonamesFile;
    }

    public void setGeonamesFile(File geonamesFile) {
      this.geonamesFile = geonamesFile;
    }

    @Description("Sub-type to assign to annotations, or null")
    public String getSubType() {
      return subType;
    }

    public void setSubType(String subType) {
      this.subType = subType;
    }

    @Description("Entries in GeoNames under this size will be excluded")
    public int getMinimumPopulation() {
      return minimumPopulation;
    }

    public void setMinimumPopulation(int minimumPopulation) {
      this.minimumPopulation = minimumPopulation;
    }
  }
}
