/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.helpers.WithType;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.elasticsearch.ElasticsearchSettings;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ComponentName("Elasticsearch Sink - Simple Span")
@ComponentDescription(
    "Persists processed items into Elasticsearch, using a simple schema where only properties, content, and span values are retained (i.e. no span properties or metadata)")
@ComponentTags("elasticsearch")
@SettingsClass(SimpleSpanElasticsearchSink.Settings.class)
public class SimpleSpanElasticsearchSink
    extends AbstractProcessorDescriptor<
        SimpleSpanElasticsearchSink.Processor, SimpleSpanElasticsearchSink.Settings> {

  @Override
  protected Processor createComponent(Context context, SimpleSpanElasticsearchSink.Settings settings) {
    return new Processor(List.of(settings.getHost()), settings.getIndex(), settings.isIgnoreCase());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations("*", SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractElasticsearchSink {
    public static final String ANNOTATIONS_FIELD = "annotations";
    public static final String CONTENT_FIELD = "content";
    public static final String PARENT_FIELD = "parent";
    public static final String PROPERTIES_FIELD = "properties";
    public static final String TYPE_FIELD = "type";

    private final boolean ignoreCase;

    public Processor(List<HttpHost> hosts, String index, boolean ignoreCase) {
      super(hosts, index);
      this.ignoreCase = ignoreCase;
    }

    @Override
    protected List<IndexRequest> itemToIndexRequests(Item item) {
      List<IndexRequest> indexRequests = new ArrayList<>();

      // Create document to capture Item
      indexRequests.add(new IndexRequest(index).id(item.getId()).source(itemToMap(item)));

      // Create documents to capture all the Text views
      item.getContents(Text.class).map(this::textToIndexRequest).forEach(indexRequests::add);

      return indexRequests;
    }

    protected static Map<String, Object> itemToMap(Item item) {
      Map<String, Object> m = new HashMap<>();

      m.put(TYPE_FIELD, "Item");
      m.put(PROPERTIES_FIELD, item.getProperties().getAll());

      return m;
    }

    protected IndexRequest textToIndexRequest(Text text) {
      return new IndexRequest(index).id(text.getId()).source(textToMap(text, ignoreCase));
    }

    protected static Map<String, Object> textToMap(Text text, boolean ignoreCase) {
      Map<String, Object> m = new HashMap<>();
      m.put(TYPE_FIELD, "Text");
      m.put(CONTENT_FIELD, text.getData());
      m.put(PARENT_FIELD, text.getItem().getId());
      m.put(PROPERTIES_FIELD, text.getProperties().getAll());

      m.put(
          ANNOTATIONS_FIELD,
          text.getAnnotations()
              .getAll()
              .collect(
                  Collectors.groupingBy(
                      WithType::getType,
                      Collectors.mapping(
                          a -> getText(text, a, ignoreCase),
                          Collectors.toSet()))));

      return m;
    }

    private static String getText(Text text, Annotation a, boolean ignoreCase){
      String s = text.getText(a).orElse("** OUT OF BOUNDS **");

      if(ignoreCase)
        s = s.toUpperCase();

      return s;
    }
  }

  public static class Settings extends ElasticsearchSettings {
    private boolean ignoreCase = false;

    @Description("Should annotation values be considered case insensitively when compiling the list of values. If true, all values will be upper cased.")
    public boolean isIgnoreCase() {
      return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
      this.ignoreCase = ignoreCase;
    }
  }
}