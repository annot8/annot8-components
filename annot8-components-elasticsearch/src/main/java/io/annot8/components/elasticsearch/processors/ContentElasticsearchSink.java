/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
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

@ComponentName("Elasticsearch Sink - Content")
@ComponentDescription("Persists content into Elasticsearch")
@ComponentTags({"content", "elasticsearch"})
@SettingsClass(ElasticsearchSettings.class)
public class ContentElasticsearchSink
    extends AbstractProcessorDescriptor<ContentElasticsearchSink.Processor, ElasticsearchSettings> {

  @Override
  protected Processor createComponent(Context context, ElasticsearchSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesContent(Content.class).build();
  }

  public static class Processor extends AbstractElasticsearchSink {

    public Processor(ElasticsearchSettings settings) {
      super(settings);
    }

    @Override
    protected List<IndexOperation<?>> itemToIndexRequests(Item item) {
      List<IndexOperation<?>> indexRequests = new ArrayList<>();

      item.getContents()
          .map(
              c -> {
                Map<String, Object> m = ElasticsearchUtils.contentToMap(c, forceString);
                m.put(ElasticsearchUtils.ITEM_ID, c.getItem().getId());

                return new IndexOperation.Builder<>()
                    .index(index)
                    .id(c.getId())
                    .document(m)
                    .build();
              })
          .forEach(indexRequests::add);

      return indexRequests;
    }

    @Override
    protected Optional<Map<String, Property>> getMapping() {
      Map<String, Property> m = ElasticsearchUtils.contentMapping();
      m.put(ElasticsearchUtils.ITEM_ID, ElasticsearchUtils.TYPE_KEYWORD);

      return Optional.of(m);
    }
  }
}
