/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.xcontent.XContentType;

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
        if (client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT)) {
          log().warn("Index {} already exists - mapping will not be applied", index);
        } else {
          log().info("Creating index {}", index);
          CreateIndexResponse createResponse =
              client.indices().create(new CreateIndexRequest(index), RequestOptions.DEFAULT);

          if (!createResponse.isAcknowledged()) {
            log().warn("Server did not acknowledge creation index {}", index);
          }

          // Apply our own logic for creating the mapping here,
          // as it is dependent on configuration so we can't use
          // approach in AbstractElasticsearchSink

          log().info("Creating mapping for index {}", index);

          String file = settings.isUseNested() ? "nesNestedMapping" : "nesMapping";
          file += settings.isForceString() ? "String.json" : ".json";

          String mapping =
              new BufferedReader(
                      new InputStreamReader(
                          NestedElasticsearchSink.class.getResourceAsStream(file)))
                  .lines()
                  .collect(Collectors.joining("\n"));

          AcknowledgedResponse mappingResponse =
              client
                  .indices()
                  .putMapping(
                      new PutMappingRequest(index).source(mapping, XContentType.JSON),
                      RequestOptions.DEFAULT);

          if (!mappingResponse.isAcknowledged()) {
            log().warn("Server did not acknowledge creation of mapping for index {}", index);
          }
        }
      } catch (IOException e) {
        log().error("An exception occurred whilst creating a mapping for index {}", index, e);
      }
    }

    @Override
    protected List<IndexRequest> itemToIndexRequests(Item item) {
      IndexRequest ir =
          new IndexRequest(index).source(transformItem(item, forceString)).id(item.getId());

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
