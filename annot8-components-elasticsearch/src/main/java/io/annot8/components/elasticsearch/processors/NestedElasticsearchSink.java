/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.references.AnnotationReference;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.elasticsearch.ElasticsearchSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;

@ComponentName("Elasticsearch Sink - Nested")
@ComponentDescription("Persists processed items into Elasticsearch, using a nested structure")
@ComponentTags("elasticsearch")
@SettingsClass(ElasticsearchSettings.class)
public class NestedElasticsearchSink
    extends AbstractProcessorDescriptor<NestedElasticsearchSink.Processor, ElasticsearchSettings> {

  @Override
  protected Processor createComponent(Context context, ElasticsearchSettings settings) {
    return new Processor(List.of(settings.getHost()), settings.getIndex());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations("*", SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractElasticsearchSink {
    public static final String ANNOTATIONS = "annotations";
    public static final String ANNOTATION_ID = "annotationId";
    public static final String BEGIN = "begin";
    public static final String BOUNDS_TYPE = "boundsType";
    public static final String CONTENT = "content";
    public static final String CONTENTS = "contents";
    public static final String CONTENT_ID = "contentId";
    public static final String CONTENT_TYPE = "contentType";
    public static final String DESCRIPTION = "description";
    public static final String END = "end";
    public static final String GROUPS = "groups";
    public static final String ID = "id";
    public static final String PARENT = "parent";
    public static final String PROPERTIES = "properties";
    public static final String ROLES = "roles";
    public static final String TYPE = "type";
    public static final String VALUE = "value";

    public Processor(List<HttpHost> hosts, String index) {
      super(hosts, index);
    }

    @Override
    protected List<IndexRequest> itemToIndexRequests(Item item) {
      IndexRequest ir = new IndexRequest(index).source(transformItem(item)).id(item.getId());

      return List.of(ir);
    }

    /**
     * Transforms an Annot8 Item into a Map with the following format: <code>{
     *   "parent": parentId
     *   "properties: {}
     *   "contents": [
     *     {
     *       "id": contentId
     *       "contentType": contentType
     *       "description": contentDescription
     *       "content": content
     *       "properties": {}
     *       "annotations": [
     *         {
     *           "id": entityId
     *           "type": entityType
     *           "boundsType": entityBoundsType
     *           "value": entityValue
     *           "begin": entityBegin
     *           "end": entityEnd
     *           "properties": {}
     *         }
     *       ]
     *     }
     *   ]
     *   "groups": [
     *     {
     *       "id": groupId
     *       "type": groupType
     *       "properties: {}
     *       "roles": {
     *         "ROLE_TYPE": [
     *           {
     *             "annotationId": annotationId
     *             "contentId": contentId
     *           }
     *         ]
     *       }
     *     }
     *   ]
     * }</code>
     */
    protected static Map<String, Object> transformItem(Item item) {
      Map<String, Object> m = new HashMap<>();

      item.getParent().ifPresent(parent -> m.put(PARENT, parent));

      Map<String, Object> itemProps = item.getProperties().getAll();
      if (!itemProps.isEmpty()) m.put(PROPERTIES, itemProps);

      // Contents
      List<Map<String, Object>> contents = new ArrayList<>();

      item.getContents()
          .forEach(
              c -> {
                Map<String, Object> mc = new HashMap<>();

                mc.put(ID, c.getId());

                if (!c.getDescription().isBlank()) mc.put(DESCRIPTION, c.getDescription());

                mc.put(CONTENT_TYPE, c.getDataClass().getName());

                if (persistData(c.getDataClass())) mc.put(CONTENT, c.getData());

                Map<String, Object> contentProps = c.getProperties().getAll();
                if (!contentProps.isEmpty()) mc.put(PROPERTIES, contentProps);

                // Annotations
                List<Map<String, Object>> annotations = new ArrayList<>();

                c.getAnnotations()
                    .getAll()
                    .forEach(
                        a -> {
                          Map<String, Object> ma = new HashMap<>();

                          ma.put(ID, a.getId());
                          ma.put(TYPE, a.getType());
                          ma.put(BOUNDS_TYPE, a.getBounds().getClass().getName());

                          Map<String, Object> annotationProps = a.getProperties().getAll();
                          if (!annotationProps.isEmpty()) ma.put(PROPERTIES, annotationProps);

                          if (a.getBounds() instanceof SpanBounds) {
                            SpanBounds sb = (SpanBounds) a.getBounds();
                            ma.put(BEGIN, sb.getBegin());
                            ma.put(END, sb.getEnd());

                            if (persistData(c.getDataClass()))
                              sb.getData(c).ifPresent(value -> ma.put(VALUE, value));
                          }

                          annotations.add(ma);
                        });

                mc.put(ANNOTATIONS, annotations);

                contents.add(mc);
              });

      m.put(CONTENTS, contents);

      // Groups
      List<Map<String, Object>> groups = new ArrayList<>();
      item.getGroups()
          .getAll()
          .forEach(
              g -> {
                Map<String, Object> mg = new HashMap<>();

                mg.put(ID, g.getId());
                mg.put(TYPE, g.getType());

                Map<String, Object> groupProps = g.getProperties().getAll();
                if (!groupProps.isEmpty()) mg.put(PROPERTIES, groupProps);

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

                groups.add(mg);
              });
      m.put(GROUPS, groups);

      return m;
    }

    protected static boolean persistData(Class<?> dataClass) {
      return String.class.isAssignableFrom(dataClass)
          || Number.class.isAssignableFrom(dataClass)
          || Boolean.class.isAssignableFrom(dataClass);
    }
  }
}
