/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import co.elastic.clients.elasticsearch._types.mapping.DynamicTemplate;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.components.elasticsearch.ElasticsearchSettings;
import io.annot8.components.elasticsearch.ElasticsearchUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ComponentName("Elasticsearch Sink - Nested")
@ComponentDescription("Persists processed items into Elasticsearch, using a nested structure")
@ComponentTags("elasticsearch")
@SettingsClass(NestedElasticsearchSink.Settings.class)
public class NestedElasticsearchSink
    extends AbstractProcessorDescriptor<
        NestedElasticsearchSink.Processor, NestedElasticsearchSink.Settings> {

  @Override
  protected Processor createComponent(Context context, NestedElasticsearchSink.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withProcessesAnnotations("*", Bounds.class)
        .withProcessesGroups("*")
        .build();
  }

  public static class Processor extends AbstractElasticsearchSink {
    public static final String ANNOTATIONS = "annotations";
    public static final String CONTENTS = "contents";
    public static final String GROUPS = "groups";

    public Processor(NestedElasticsearchSink.Settings settings) {
      super(settings);

      // Create mapping
      try {
        if (client.indices().exists(r -> r.index(index)).value()) {
          log().warn("Index {} already exists - mapping will not be applied", index);
        } else {
          log().info("Creating index {}", index);
          CreateIndexResponse createResponse = client.indices().create(r -> r.index(index));

          if (Boolean.FALSE.equals(createResponse.acknowledged())) {
            log().warn("Server did not acknowledge creation index {}", index);
          }

          // Apply our own logic for creating the mapping here,
          // as it is dependent on configuration so we can't use
          // approach in AbstractElasticsearchSink

          log().info("Creating mapping for index {}", index);

          PutMappingResponse mappingResponse =
              client
                  .indices()
                  .putMapping(
                      r ->
                          r.index(index)
                              .properties(createMapping(settings.isUseNested()))
                              .dynamicTemplates(
                                  createDynamicTemplate(
                                      settings.isUseNested(), settings.isForceString())));

          if (!mappingResponse.acknowledged()) {
            log().warn("Server did not acknowledge creation of mapping for index {}", index);
          }
        }
      } catch (IOException e) {
        log().error("An exception occurred whilst creating a mapping for index {}", index, e);
      }
    }

    @Override
    protected List<IndexOperation<?>> itemToIndexRequests(Item item) {
      return List.of(
          new IndexOperation.Builder<>()
              .index(index)
              .id(item.getId())
              .document(transformItem(item, forceString))
              .build());
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
    protected static Map<String, Object> transformItem(Item item, boolean forceString) {
      Map<String, Object> m = ElasticsearchUtils.itemToMap(item, forceString);

      // Contents
      List<Map<String, Object>> contents = new ArrayList<>();

      item.getContents()
          .forEach(
              c -> {
                Map<String, Object> mc = ElasticsearchUtils.contentToMap(c, forceString);

                // Annotations
                List<Map<String, Object>> annotations = new ArrayList<>();

                c.getAnnotations()
                    .getAll()
                    .map(a -> ElasticsearchUtils.annotationToMap(a, c, forceString))
                    .forEach(annotations::add);

                mc.put(ANNOTATIONS, annotations);

                contents.add(mc);
              });

      m.put(CONTENTS, contents);

      // Groups
      List<Map<String, Object>> groups = new ArrayList<>();
      item.getGroups()
          .getAll()
          .map(g -> ElasticsearchUtils.groupToMap(g, forceString))
          .forEach(groups::add);

      m.put(GROUPS, groups);

      return m;
    }

    private static Map<String, Property> createMapping(boolean nested) {
      Map<String, Property> mapping = new HashMap<>();

      mapping.put(ElasticsearchUtils.ID, ElasticsearchUtils.TYPE_KEYWORD);

      Property contents;
      Property groups;
      if (nested) {
        contents =
            Property.of(
                p ->
                    p.nested(
                        n ->
                            n.properties(ElasticsearchUtils.CONTENT, ElasticsearchUtils.TYPE_TEXT)
                                .properties(
                                    ElasticsearchUtils.CONTENT_TYPE,
                                    ElasticsearchUtils.TYPE_KEYWORD)
                                .properties(
                                    ElasticsearchUtils.DESCRIPTION, ElasticsearchUtils.TYPE_TEXT)
                                .properties(ElasticsearchUtils.ID, ElasticsearchUtils.TYPE_KEYWORD)
                                .properties(
                                    ANNOTATIONS,
                                    a ->
                                        a.nested(
                                            na ->
                                                na.properties(
                                                        ElasticsearchUtils.BEGIN,
                                                        ElasticsearchUtils.TYPE_LONG)
                                                    .properties(
                                                        ElasticsearchUtils.END,
                                                        ElasticsearchUtils.TYPE_LONG)
                                                    .properties(
                                                        ElasticsearchUtils.BOUNDS_TYPE,
                                                        ElasticsearchUtils.TYPE_KEYWORD)
                                                    .properties(
                                                        ElasticsearchUtils.ID,
                                                        ElasticsearchUtils.TYPE_KEYWORD)
                                                    .properties(
                                                        ElasticsearchUtils.TYPE,
                                                        ElasticsearchUtils.TYPE_KEYWORD)
                                                    .properties(
                                                        ElasticsearchUtils.GEO,
                                                        ElasticsearchUtils.TYPE_GEOSHAPE)
                                                    .properties(
                                                        ElasticsearchUtils.VALUE,
                                                        ElasticsearchUtils
                                                            .TYPE_TEXT_WITH_KEYWORD)))));

        groups =
            Property.of(
                p ->
                    p.nested(
                        n ->
                            n.properties(ElasticsearchUtils.ID, ElasticsearchUtils.TYPE_KEYWORD)
                                .properties(
                                    ElasticsearchUtils.TYPE, ElasticsearchUtils.TYPE_KEYWORD)));
      } else {
        contents =
            Property.of(
                p ->
                    p.object(
                        o ->
                            o.properties(ElasticsearchUtils.CONTENT, ElasticsearchUtils.TYPE_TEXT)
                                .properties(
                                    ElasticsearchUtils.CONTENT_TYPE,
                                    ElasticsearchUtils.TYPE_KEYWORD)
                                .properties(
                                    ElasticsearchUtils.DESCRIPTION, ElasticsearchUtils.TYPE_TEXT)
                                .properties(ElasticsearchUtils.ID, ElasticsearchUtils.TYPE_KEYWORD)
                                .properties(
                                    ANNOTATIONS,
                                    a ->
                                        a.object(
                                            oa ->
                                                oa.properties(
                                                        ElasticsearchUtils.BEGIN,
                                                        ElasticsearchUtils.TYPE_LONG)
                                                    .properties(
                                                        ElasticsearchUtils.END,
                                                        ElasticsearchUtils.TYPE_LONG)
                                                    .properties(
                                                        ElasticsearchUtils.BOUNDS_TYPE,
                                                        ElasticsearchUtils.TYPE_KEYWORD)
                                                    .properties(
                                                        ElasticsearchUtils.ID,
                                                        ElasticsearchUtils.TYPE_KEYWORD)
                                                    .properties(
                                                        ElasticsearchUtils.TYPE,
                                                        ElasticsearchUtils.TYPE_KEYWORD)
                                                    .properties(
                                                        ElasticsearchUtils.GEO,
                                                        ElasticsearchUtils.TYPE_GEOSHAPE)
                                                    .properties(
                                                        ElasticsearchUtils.VALUE,
                                                        ElasticsearchUtils
                                                            .TYPE_TEXT_WITH_KEYWORD)))));

        groups =
            Property.of(
                p ->
                    p.object(
                        o ->
                            o.properties(ElasticsearchUtils.ID, ElasticsearchUtils.TYPE_KEYWORD)
                                .properties(
                                    ElasticsearchUtils.TYPE, ElasticsearchUtils.TYPE_KEYWORD)));
      }

      mapping.put(CONTENTS, contents);
      mapping.put(GROUPS, groups);

      return mapping;
    }

    private static List<Map<String, DynamicTemplate>> createDynamicTemplate(
        boolean nested, boolean forceString) {
      List<Map<String, DynamicTemplate>> l = new ArrayList<>();
      Map<String, DynamicTemplate> groupRolesContent = new HashMap<>();
      groupRolesContent.put(
          "group_roles_content",
          DynamicTemplate.of(
              d ->
                  d.pathMatch("groups.roles.*.contentId")
                      .mapping(ElasticsearchUtils.TYPE_KEYWORD)));
      l.add(groupRolesContent);

      Map<String, DynamicTemplate> groupRolesAnnotation = new HashMap<>();
      groupRolesAnnotation.put(
          "group_roles_annotation",
          DynamicTemplate.of(
              d ->
                  d.pathMatch("groups.roles.*.annotationId")
                      .mapping(ElasticsearchUtils.TYPE_KEYWORD)));
      l.add(groupRolesAnnotation);

      if (forceString) {
        Map<String, DynamicTemplate> stringProperties = new HashMap<>();
        stringProperties.put(
            "string_properties",
            DynamicTemplate.of(
                d -> d.pathMatch("*.properties.*").mapping(ElasticsearchUtils.TYPE_TEXT)));
        l.add(stringProperties);
      }

      if (nested) {
        Map<String, DynamicTemplate> groupRoles = new HashMap<>();
        groupRoles.put(
            "group_roles",
            DynamicTemplate.of(
                d ->
                    d.pathMatch("groups.roles.*.annotationId")
                        .mapping(ElasticsearchUtils.TYPE_KEYWORD)));
        l.add(groupRoles);
      }

      return l;
    }
  }

  public static class Settings extends ElasticsearchSettings {
    private boolean useNested = false;

    @Description(
        value = "Should the 'nested' type be used for arrays within Elasticsearch?",
        defaultValue = "false")
    public boolean isUseNested() {
      return useNested;
    }

    public void setUseNested(boolean useNested) {
      this.useNested = useNested;
    }
  }
}
