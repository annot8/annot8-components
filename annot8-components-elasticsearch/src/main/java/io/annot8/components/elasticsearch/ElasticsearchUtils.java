/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  public static final Property TYPE_GEOSHAPE = Property.of(p -> p.geoShape(g -> g));
  public static final Property TYPE_INTEGER = Property.of(p -> p.integer(i -> i));
  public static final Property TYPE_KEYWORD = Property.of(p -> p.keyword(k -> k));
  public static final Property TYPE_LONG = Property.of(p -> p.long_(l -> l));
  public static final Property TYPE_TEXT = Property.of(p -> p.text(t -> t));
  public static final Property TYPE_TEXT_WITH_KEYWORD =
      Property.of(
          p ->
              p.text(
                  t -> t.fields("keyword", Property.of(q -> q.keyword(k -> k.ignoreAbove(256))))));

  private static final ObjectMapper mapper = new ObjectMapper();

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
        Map<String, Object> json =
            mapper.readValue(
                a.getProperties()
                    .get(PropertyKeys.PROPERTY_KEY_GEOJSON, String.class)
                    .orElseThrow(),
                new TypeReference<>() {});

        ma.put(GEO, json);

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
              .orElseThrow()
              .doubleValue();
      double lon =
          a.getProperties()
              .get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Number.class)
              .orElseThrow()
              .doubleValue();

      if (lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0) {
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

  public static Map<String, Property> annotationMapping() {
    Map<String, Property> m = new HashMap<>();
    m.put(ID, TYPE_KEYWORD);
    m.put(TYPE, TYPE_KEYWORD);
    m.put(BOUNDS_TYPE, TYPE_KEYWORD);
    m.put(GEO, TYPE_GEOSHAPE);
    m.put(BEGIN, TYPE_INTEGER);
    m.put(END, TYPE_INTEGER);

    // TODO: What should VALUE be mapped as?

    // Don't know in advance what properties annotations will have,
    // so this will have to be mapped dynamically by Elasticsearch

    return m;
  }

  public static Map<String, Object> contentToMap(Content<?> c, boolean forceString) {
    Map<String, Object> mc = new HashMap<>();

    mc.put(ID, c.getId());
    mc.put(CONTENT_TYPE, c.getDataClass().getName());

    if (c.getDescription() != null && !c.getDescription().isBlank())
      mc.put(DESCRIPTION, c.getDescription());

    if (shouldPersistData(c.getDataClass())) mc.put(CONTENT, c.getData());

    Map<String, Object> contentProps = c.getProperties().getAll();
    if (!contentProps.isEmpty())
      mc.put(PROPERTIES, forceString ? toStringMap(contentProps) : contentProps);

    return mc;
  }

  public static Map<String, Property> contentMapping() {
    Map<String, Property> m = new HashMap<>();
    m.put(ID, TYPE_KEYWORD);
    m.put(CONTENT_TYPE, TYPE_KEYWORD);
    m.put(DESCRIPTION, TYPE_TEXT);

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
    for (Entry<String, Stream<AnnotationReference>> role : mar.entrySet()) {
      List<Map<String, String>> lr =
          role.getValue()
              .map(
                  r -> {
                    Map<String, String> mr = new HashMap<>();

                    mr.put(CONTENT_ID, r.getContentId());
                    mr.put(ANNOTATION_ID, r.getAnnotationId());

                    return mr;
                  })
              .collect(Collectors.toList());

      roles.put(role.getKey(), lr);
    }

    mg.put(ROLES, roles);

    return mg;
  }

  public static Map<String, Property> groupMapping() {
    Map<String, Property> m = new HashMap<>();
    m.put(ID, TYPE_KEYWORD);
    m.put(TYPE, TYPE_KEYWORD);

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

  public static Map<String, Property> itemMapping() {
    Map<String, Property> m = new HashMap<>();
    m.put(ID, TYPE_KEYWORD);
    m.put(PARENT, TYPE_KEYWORD);

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

  @SuppressWarnings("unchecked")
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
