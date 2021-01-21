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
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.components.elasticsearch.ElasticsearchSettings;
import io.annot8.components.elasticsearch.ElasticsearchUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.elasticsearch.action.index.IndexRequest;

@ComponentName("Elasticsearch Sink - Annotations")
@ComponentDescription("Persists annotations into Elasticsearch")
@ComponentTags({"annotations", "elasticsearch"})
@SettingsClass(ElasticsearchSettings.class)
public class AnnotationElasticsearchSink
    extends AbstractProcessorDescriptor<
        AnnotationElasticsearchSink.Processor, ElasticsearchSettings> {

  @Override
  protected Processor createComponent(Context context, ElasticsearchSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withProcessesAnnotations("*", Bounds.class)
        .build();
  }

  public static class Processor extends AbstractElasticsearchSink {

    public Processor(ElasticsearchSettings settings) {
      super(settings);
    }

    @Override
    protected List<IndexRequest> itemToIndexRequests(Item item) {
      List<IndexRequest> indexRequests = new ArrayList<>();

      item.getContents()
          .forEach(
              c ->
                  c.getAnnotations()
                      .getAll()
                      .map(
                          a -> {
                            Map<String, Object> m =
                                ElasticsearchUtils.annotationToMap(a, c, forceString);
                            m.put(ElasticsearchUtils.CONTENT_ID, c.getId());
                            m.put(ElasticsearchUtils.ITEM_ID, c.getItem().getId());

                            return new IndexRequest(index).id(a.getId()).source(m);
                          })
                      .forEach(indexRequests::add));

      return indexRequests;
    }

    @Override
    protected Optional<Map<String, Object>> getMapping() {
      Map<String, Object> m = ElasticsearchUtils.annotationMapping();
      m.put(ElasticsearchUtils.CONTENT_ID, ElasticsearchUtils.mappingType("keyword"));
      m.put(ElasticsearchUtils.ITEM_ID, ElasticsearchUtils.mappingType("keyword"));

      return Optional.of(ElasticsearchUtils.wrapWithProperties(m));
    }
  }
}
