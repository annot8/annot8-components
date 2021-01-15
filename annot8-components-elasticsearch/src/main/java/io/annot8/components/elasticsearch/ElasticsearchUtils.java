/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.references.AnnotationReference;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.conventions.PropertyKeys;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.elasticsearch.common.geo.GeoUtils;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;

/**
 * Contains common constants and utilities for manipulating Annot8 items ready for persisting in to
 * Elasticsearch.
 */
public class ElasticsearchUtils {

  public static final String ANNOTATION_ID = "annotationId";
  public static final String BEGIN = "begin";
  public static final String BOUNDS_TYPE = "boundsType";
  public static final String CONTENT = "content";
  public static final String CONTENT_ID = "contentId";
  public static final String CONTENT_TYPE = "contentType";
  public static final String DESCRIPTION = "description";
  public static final String END = "end";
  public static final String GEO = "geo";
  public static final String ID = "id";
  public static final String ITEM_ID = "itemId";
  public static final String PARENT = "parent";
  public static final String PROPERTIES = "properties";
  public static final String ROLES = "roles";
  public static final String TYPE = "type";
  public static final String VALUE = "value";

  private ElasticsearchUtils() {
    // Private constructor for utility class
  }

  public static Map<String, Object> annotationToMap(
      Annotation a, Content<?> c, boolean forceString) {
    Map<String, Object> ma = new HashMap<>();

    ma.put(ID, a.getId());
    ma.put(TYPE, a.getType());
    ma.put(BOUNDS_TYPE, a.getBounds().getClass().getName());

    Map<String, Object> annotationProps = a.getProperties().getAll();
    if (!annotationProps.isEmpty())
      ma.put(PROPERTIES, forceString ? toStringMap(annotationProps) : annotationProps);

    boolean geoJsonSucceeded = false;

    if (a.getProperties().has(PropertyKeys.PROPERTY_KEY_GEOJSON, String.class)) {
      try {
        XContentParser parser =
            JsonXContent.jsonXContent.createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                a.getProperties().get(PropertyKeys.PROPERTY_KEY_GEOJSON, String.class).get());

        ma.put(GEO, parser.map());

        geoJsonSucceeded = true;
      } catch (Exception e) {
        // Ought to log this or something, but no access to logger here
      }
    }

    if (!geoJsonSucceeded
        && a.getProperties().has(PropertyKeys.PROPERTY_KEY_LATITUDE, Number.class)
        && a.getProperties().has(PropertyKeys.PROPERTY_KEY_LONGITUDE, Number.class)) {
      double lat =
          a.getProperties()
              .get(PropertyKeys.PROPERTY_KEY_LATITUDE, Number.class)
              .get()
              .doubleValue();
      double lon =
          a.getProperties()
              .get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Number.class)
              .get()
              .doubleValue();

      if (GeoUtils.isValidLatitude(lat) && GeoUtils.isValidLongitude(lon)) {
        Map<String, Object> geojson = new HashMap<>();
        geojson.put("type", "Point");
        geojson.put("coordinates", List.of(lon, lat));

        ma.put(GEO, geojson);
      }
    }

    if (a.getBounds() instanceof SpanBounds) {
      SpanBounds sb = (SpanBounds) a.getBounds();
      ma.put(BEGIN, sb.getBegin());
      ma.put(END, sb.getEnd());

      if (shouldPersistData(c.getDataClass()))
        sb.getData(c).ifPresent(value -> ma.put(VALUE, value));
    }

    return ma;
  }

  public static Map<String, Object> annotationMapping() {
    Map<String, Object> m = new HashMap<>();
    m.put(ID, mappingType("keyword"));
    m.put(TYPE, mappingType("keyword"));
    m.put(BOUNDS_TYPE, mappingType("keyword"));
    m.put(GEO, mappingType("geo_shape"));
    m.put(BEGIN, mappingType("integer"));
    m.put(END, mappingType("integer"));

    // TODO: What should VALUE be mapped as?

    // Don't know in advance what properties annotations will have,
    // so this will have to be mapped dynamically by Elasticsearch

    return m;
  }

  public static Map<String, Object> contentToMap(Content<?> c, boolean forceString) {
    Map<String, Object> mc = new HashMap<>();

    mc.put(ID, c.getId());
    mc.put(CONTENT_TYPE, c.getDataClass().getName());

    if (!c.getDescription().isBlank()) mc.put(DESCRIPTION, c.getDescription());

    if (shouldPersistData(c.getDataClass())) mc.put(CONTENT, c.getData());

    Map<String, Object> contentProps = c.getProperties().getAll();
    if (!contentProps.isEmpty())
      mc.put(PROPERTIES, forceString ? toStringMap(contentProps) : contentProps);

    return mc;
  }

  public static Map<String, Object> contentMapping() {
    Map<String, Object> m = new HashMap<>();
    m.put(ID, mappingType("keyword"));
    m.put(CONTENT_TYPE, mappingType("keyword"));
    m.put(DESCRIPTION, mappingType("text"));

    // TODO: What should CONTENT be mapped as?

    // Don't know in advance what properties annotations will have,
    // so this will have to be mapped dynamically by Elasticsearch

    return m;
  }

  public static Map<String, Object> groupToMap(Group g, boolean forceString) {
    Map<String, Object> mg = new HashMap<>();

    mg.put(ID, g.getId());
    mg.put(TYPE, g.getType());

    Map<String, Object> groupProps = g.getProperties().getAll();
    if (!groupProps.isEmpty())
      mg.put(PROPERTIES, forceString ? toStringMap(groupProps) : groupProps);

    Map<String, List<Map<String, String>>> roles = new HashMap<>();

    Map<String, Stream<AnnotationReference>> mar = g.getReferences();
    for (String role : mar.keySet()) {
      List<Map<String, String>> lr =
          mar.get(role)
              .map(
                  r -> {
                    Map<String, String> mr = new HashMap<>();

                    mr.put(CONTENT_ID, r.getContentId());
                    mr.put(ANNOTATION_ID, r.getAnnotationId());

                    return mr;
                  })
              .collect(Collectors.toList());

      roles.put(role, lr);
    }

    mg.put(ROLES, roles);

    return mg;
  }

  public static Map<String, Object> groupMapping() {
    Map<String, Object> m = new HashMap<>();
    m.put(ID, mappingType("keyword"));
    m.put(TYPE, mappingType("keyword"));

    // TODO: Should add dynamic mapping for roles

    // Don't know in advance what properties annotations will have,
    // so this will have to be mapped dynamically by Elasticsearch

    return m;
  }

  public static Map<String, Object> itemToMap(Item i, boolean forceString) {
    Map<String, Object> m = new HashMap<>();

    m.put(ID, i.getId());
    i.getParent().ifPresent(parent -> m.put(PARENT, parent));

    Map<String, Object> itemProps = i.getProperties().getAll();
    if (!itemProps.isEmpty()) m.put(PROPERTIES, forceString ? toStringMap(itemProps) : itemProps);

    return m;
  }

  public static Map<String, Object> itemMapping() {
    Map<String, Object> m = new HashMap<>();
    m.put(ID, mappingType("keyword"));
    m.put(PARENT, mappingType("keyword"));

    // Don't know in advance what properties annotations will have,
    // so this will have to be mapped dynamically by Elasticsearch

    return m;
  }

  /**
   * Is the provided data class suitable for persisting into Elasticsearch?
   *
   * @param dataClass
   * @return
   */
  public static boolean shouldPersistData(Class<?> dataClass) {
    return String.class.isAssignableFrom(dataClass)
        || Number.class.isAssignableFrom(dataClass)
        || Boolean.class.isAssignableFrom(dataClass);
  }

  public static Map<String, Object> wrapWithProperties(Map<String, Object> m) {
    Map<String, Object> wrapper = new HashMap<>();
    wrapper.put("properties", m);

    return wrapper;
  }

  public static Map<String, Object> mappingType(String type) {
    Map<String, Object> m = new HashMap<>();
    m.put("type", type);

    return m;
  }

  public static Map<String, Object> toStringMap(Map<String, Object> map) {
    Map<String, Object> m = new HashMap<>();

    map.forEach(
        (key, value) -> {
          if (value instanceof Map) {
            try {
              Map<String, Object> me = (Map<String, Object>) value;
              m.put(key, toStringMap(me));
            } catch (ClassCastException cce) {
              // Do nothing - could log?
            }
          } else {
            m.put(key, value.toString());
          }
        });

    return m;
  }
}
