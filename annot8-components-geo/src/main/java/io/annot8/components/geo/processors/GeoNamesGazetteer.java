/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import com.opencsv.bean.*;
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
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
              loadGazetteer(
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

  private Map<Set<String>, Map<String, Object>> loadGazetteer(
      File f,
      Settings.GeoNamesAdditionalProperties additionalProperties,
      boolean geoJson,
      int minPopulation)
      throws IOException {
    Map<Set<String>, Map<String, Object>> gazetteer = new HashMap<>();

    try (FileReader reader = new FileReader(f)) {
      CsvToBean<GeoNamesBean> csvToBean =
          new CsvToBeanBuilder<GeoNamesBean>(reader)
              .withSeparator('\t')
              .withIgnoreQuotations(true)
              .withType(GeoNamesBean.class)
              .build();

      for (GeoNamesBean bean : csvToBean) {
        if (bean.getPopulation() == null) {
          if (minPopulation > 0) // Only keep null population if minPopulation is 0
          continue;
        } else {
          if (bean.getPopulation() < minPopulation) continue;
        }

        Set<String> names = new HashSet<>();
        names.add(bean.getName());
        names.add(bean.getAsciiName());
        names.addAll(bean.getAlternateNames());

        Map<String, Object> props = new HashMap<>();

        switch (additionalProperties) {
          case ALL:
            // GeoNames metadata
            putIfNotNull(props, PropertyKeys.PROPERTY_KEY_IDENTIFIER, bean.getGeonameId());
            putIfNotNull(props, "lastUpdated", bean.getModificationDate());
          case EXTENDED:
            // More detailed information
            putIfNotNull(props, "admin1Code", bean.getAdmin1Code());
            putIfNotNull(props, "admin2Code", bean.getAdmin2Code());
            putIfNotNull(props, "admin3Code", bean.getAdmin3Code());
            putIfNotNull(props, "admin4Code", bean.getAdmin4Code());
            putIfNotNull(props, "cc2", bean.getCc2());
            if (bean.getDem() != null && !bean.getDem().equals(-9999))
              props.put("dem", bean.getPopulation());
            putIfNotNull(props, PropertyKeys.PROPERTY_KEY_ELEVATION, bean.getElevation());
            putIfNotNull(props, "featureClass", bean.getFeatureClass());
            putIfNotNull(props, "featureCode", bean.getFeatureCode());
            putIfNotNull(props, "population", bean.getPopulation());
            putIfNotNull(props, "timezone", bean.getTimezone());
          case BASIC:
            // Essential geo information
            putIfNotNull(props, PropertyKeys.PROPERTY_KEY_COUNTRY, bean.getCountryCode());
            putIfNotNull(props, PropertyKeys.PROPERTY_KEY_LATITUDE, bean.getLatitude());
            putIfNotNull(props, PropertyKeys.PROPERTY_KEY_LONGITUDE, bean.getLongitude());
          case NONE:
          default:
            break;
        }

        if (geoJson && bean.getLatitude() != null && bean.getLongitude() != null) {
          props.put(
              PropertyKeys.PROPERTY_KEY_GEOJSON,
              "{\"type\":\"Point\",\"coordinates\":["
                  + bean.getLongitude()
                  + ","
                  + bean.getLatitude()
                  + "]}");
        }

        gazetteer.put(names, props);
      }
    }

    return gazetteer;
  }

  private void putIfNotNull(Map<String, Object> m, String k, Object v) {
    if (v != null) m.put(k, v);
  }

  private void putIfNotNull(Map<String, Object> m, String k, String s) {
    if (s != null && !s.isBlank()) m.put(k, s);
  }

  private void putIfNotNull(Map<String, Object> m, String k, List<String> l) {
    if (l == null || l.isEmpty()) return;

    List<String> fl = l.stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
    if (!fl.isEmpty()) m.put(k, fl);
  }

  public static class GeoNamesBean {
    /*
    GeoNames fields and their order:

    geonameid         : integer id of record in geonames database
    name              : name of geographical point (utf8) varchar(200)
    asciiname         : name of geographical point in plain ascii characters, varchar(200)
    alternatenames    : alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
    latitude          : latitude in decimal degrees (wgs84)
    longitude         : longitude in decimal degrees (wgs84)
    feature class     : see http://www.geonames.org/export/codes.html, char(1)
    feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
    country code      : ISO-3166 2-letter country code, 2 characters
    cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
    admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
    admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
    admin3 code       : code for third level administrative division, varchar(20)
    admin4 code       : code for fourth level administrative division, varchar(20)
    population        : bigint (8 byte int)
    elevation         : in meters, integer
    dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
    timezone          : the iana timezone id (see file timeZone.txt) varchar(40)
    modification date : date of last modification in yyyy-MM-dd format
     */

    @CsvBindByPosition(position = 0)
    private Long geonameId;

    @CsvBindByPosition(position = 1)
    private String name;

    @CsvBindByPosition(position = 2)
    private String asciiName;

    @CsvBindAndSplitByPosition(position = 3, splitOn = ",", elementType = String.class)
    private List<String> alternateNames;

    @CsvBindByPosition(position = 4)
    private Double latitude;

    @CsvBindByPosition(position = 5)
    private Double longitude;

    @CsvBindByPosition(position = 6)
    private String featureClass;

    @CsvBindByPosition(position = 7)
    private String featureCode;

    @CsvBindByPosition(position = 8)
    private String countryCode;

    @CsvBindAndSplitByPosition(position = 9, splitOn = ",", elementType = String.class)
    private List<String> cc2;

    @CsvBindByPosition(position = 10)
    private String admin1Code;

    @CsvBindByPosition(position = 11)
    private String admin2Code;

    @CsvBindByPosition(position = 12)
    private String admin3Code;

    @CsvBindByPosition(position = 13)
    private String admin4Code;

    @CsvBindByPosition(position = 14)
    private Integer population;

    @CsvBindByPosition(position = 15)
    private Integer elevation;

    @CsvBindByPosition(position = 16)
    private Integer dem;

    @CsvBindByPosition(position = 17)
    private String timezone;

    @CsvBindByPosition(position = 18)
    @CsvDate("yyyy-MM-dd")
    private LocalDate modificationDate;

    public Long getGeonameId() {
      return geonameId;
    }

    public void setGeonameId(Long geonameId) {
      this.geonameId = geonameId;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getAsciiName() {
      return asciiName;
    }

    public void setAsciiName(String asciiName) {
      this.asciiName = asciiName;
    }

    public List<String> getAlternateNames() {
      return alternateNames;
    }

    public void setAlternateNames(List<String> alternateNames) {
      this.alternateNames = alternateNames;
    }

    public Double getLatitude() {
      return latitude;
    }

    public void setLatitude(Double latitude) {
      this.latitude = latitude;
    }

    public Double getLongitude() {
      return longitude;
    }

    public void setLongitude(Double longitude) {
      this.longitude = longitude;
    }

    public String getFeatureClass() {
      return featureClass;
    }

    public void setFeatureClass(String featureClass) {
      this.featureClass = featureClass;
    }

    public String getFeatureCode() {
      return featureCode;
    }

    public void setFeatureCode(String featureCode) {
      this.featureCode = featureCode;
    }

    public String getCountryCode() {
      return countryCode;
    }

    public void setCountryCode(String countryCode) {
      this.countryCode = countryCode;
    }

    public List<String> getCc2() {
      return cc2;
    }

    public void setCc2(List<String> cc2) {
      this.cc2 = cc2;
    }

    public String getAdmin1Code() {
      return admin1Code;
    }

    public void setAdmin1Code(String admin1Code) {
      this.admin1Code = admin1Code;
    }

    public String getAdmin2Code() {
      return admin2Code;
    }

    public void setAdmin2Code(String admin2Code) {
      this.admin2Code = admin2Code;
    }

    public String getAdmin3Code() {
      return admin3Code;
    }

    public void setAdmin3Code(String admin3Code) {
      this.admin3Code = admin3Code;
    }

    public String getAdmin4Code() {
      return admin4Code;
    }

    public void setAdmin4Code(String admin4Code) {
      this.admin4Code = admin4Code;
    }

    public Integer getPopulation() {
      return population;
    }

    public void setPopulation(Integer population) {
      this.population = population;
    }

    public Integer getElevation() {
      return elevation;
    }

    public void setElevation(Integer elevation) {
      this.elevation = elevation;
    }

    public Integer getDem() {
      return dem;
    }

    public void setDem(Integer dem) {
      this.dem = dem;
    }

    public String getTimezone() {
      return timezone;
    }

    public void setTimezone(String timezone) {
      this.timezone = timezone;
    }

    public LocalDate getModificationDate() {
      return modificationDate;
    }

    public void setModificationDate(LocalDate modificationDate) {
      this.modificationDate = modificationDate;
    }
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

    public enum GeoNamesAdditionalProperties {
      NONE,
      EXTENDED,
      BASIC,
      ALL
    }
  }
}
