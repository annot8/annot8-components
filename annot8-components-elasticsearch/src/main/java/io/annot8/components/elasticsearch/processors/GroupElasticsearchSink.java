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
import io.annot8.api.data.Item;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.components.elasticsearch.ElasticsearchSettings;
import io.annot8.components.elasticsearch.ElasticsearchUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ComponentName("Elasticsearch Sink - Groups")
@ComponentDescription("Persists groups into Elasticsearch")
@ComponentTags({"groups", "elasticsearch"})
@SettingsClass(ElasticsearchSettings.class)
public class GroupElasticsearchSink
    extends AbstractProcessorDescriptor<GroupElasticsearchSink.Processor, ElasticsearchSettings> {

  @Override
  protected Processor createComponent(Context context, ElasticsearchSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesGroups("*").build();
  }

  public static class Processor extends AbstractElasticsearchSink {

    public Processor(ElasticsearchSettings settings) {
      super(settings);
    }

    @Override
    protected List<IndexOperation<?>> itemToIndexRequests(Item item) {
      List<IndexOperation<?>> indexRequests = new ArrayList<>();

      item.getGroups()
          .getAll()
          .map(
              g -> {
                Map<String, Object> m = ElasticsearchUtils.groupToMap(g, forceString);
                m.put(ElasticsearchUtils.ITEM_ID, item.getId());

                return new IndexOperation.Builder<>()
                    .index(index)
                    .id(g.getId())
                    .document(m)
                    .build();
              })
          .forEach(indexRequests::add);

      return indexRequests;
    }

    @Override
    protected Optional<Map<String, Property>> getMapping() {
      Map<String, Property> m = ElasticsearchUtils.groupMapping();
      m.put(ElasticsearchUtils.ITEM_ID, ElasticsearchUtils.TYPE_KEYWORD);

      return Optional.of(m);
    }
  }
}
