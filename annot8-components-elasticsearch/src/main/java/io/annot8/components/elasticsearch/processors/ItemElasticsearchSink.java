/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.elasticsearch.action.index.IndexRequest;

@ComponentName("Elasticsearch Sink - Item")
@ComponentDescription("Persists items into Elasticsearch")
@ComponentTags({"elasticsearch", "item"})
@SettingsClass(ElasticsearchSettings.class)
public class ItemElasticsearchSink
    extends AbstractProcessorDescriptor<ItemElasticsearchSink.Processor, ElasticsearchSettings> {

  @Override
  protected Processor createComponent(Context context, ElasticsearchSettings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  public static class Processor extends AbstractElasticsearchSink {
    public Processor(ElasticsearchSettings settings) {
      super(settings);
    }

    @Override
    protected List<IndexRequest> itemToIndexRequests(Item item) {
      return List.of(
          new IndexRequest(index)
              .id(item.getId())
              .source(ElasticsearchUtils.itemToMap(item, forceString)));
    }

    @Override
    protected Optional<Map<String, Object>> getMapping() {
      return Optional.of(
          ElasticsearchUtils.wrapWithProperties(ElasticsearchUtils.contentMapping()));
    }
  }
}
