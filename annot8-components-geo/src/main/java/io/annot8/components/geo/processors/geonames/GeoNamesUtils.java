/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors.geonames;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.annot8.conventions.PropertyKeys;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GeoNamesUtils {
  private GeoNamesUtils() {
    // Private constructor for utility class
  }

  public static Map<Set<String>, Map<String, Object>> loadGazetteer(
      File f, GeoNamesAdditionalProperties additionalProperties, boolean geoJson, int minPopulation)
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

  private static void putIfNotNull(Map<String, Object> m, String k, Object v) {
    if (v != null) m.put(k, v);
  }

  private static void putIfNotNull(Map<String, Object> m, String k, String s) {
    if (s != null && !s.isBlank()) m.put(k, s);
  }

  private static void putIfNotNull(Map<String, Object> m, String k, List<String> l) {
    if (l == null || l.isEmpty()) return;

    List<String> fl = l.stream().filter(s -> !s.isBlank()).collect(Collectors.toList());
    if (!fl.isEmpty()) m.put(k, fl);
  }
}
