/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.components.AbstractProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public abstract class AbstractElasticsearchSink extends AbstractProcessor {
  protected final RestHighLevelClient client;
  protected final String index;

  public AbstractElasticsearchSink(List<HttpHost> hosts, String index) {
    client = new RestHighLevelClient(RestClient.builder(hosts.toArray(new HttpHost[0])));

    this.index = index;
  }

  @Override
  public void close() {
    if (client != null) {
      try {
        client.close();
      } catch (IOException e) {
        log().warn("Unable to close Elasticsearch client", e);
      }
    }
  }

  @Override
  public ProcessorResponse process(Item item) {
    List<IndexRequest> requests = itemToIndexRequests(item);

    if (requests.isEmpty()) {
      log().debug("No index requests created for item {}", item.getId());
      return ProcessorResponse.ok();
    }

    BulkRequest bulkRequest = new BulkRequest();
    requests.forEach(bulkRequest::add);

    List<Exception> exceptions = new ArrayList<>();
    try {
      log()
          .debug(
              "Performing bulk request to index item {} ({} index requests)",
              item.getId(),
              requests.size());
      BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);

      for (BulkItemResponse bulkItemResponse : response) {
        if (bulkItemResponse.isFailed()) {
          BulkItemResponse.Failure failure = bulkItemResponse.getFailure();

          log()
              .error(
                  "Failed to create/update document {} in index {}: {}",
                  bulkItemResponse.getId(),
                  bulkItemResponse.getIndex(),
                  failure.getMessage(),
                  failure.getCause());
          exceptions.add(failure.getCause());

          continue;
        }

        DocWriteResponse itemResponse = bulkItemResponse.getResponse();

        if (itemResponse.getResult() == DocWriteResponse.Result.CREATED) {
          log()
              .debug(
                  "New document {} created in index {}",
                  itemResponse.getId(),
                  itemResponse.getIndex());
        } else if (itemResponse.getResult() == DocWriteResponse.Result.UPDATED) {
          log()
              .debug(
                  "Existing document {} updated in index {}",
                  itemResponse.getId(),
                  itemResponse.getIndex());
        } else {
          log()
              .error(
                  "Unexpected result returned whilst indexing document {} in index {}: {}",
                  itemResponse.getId(),
                  itemResponse.getIndex(),
                  itemResponse.getResult().name());
        }
      }
    } catch (IOException e) {
      log().error("Exception thrown whilst performing bulk request: {}", e.getMessage());
      return ProcessorResponse.itemError(e);
    }

    if (exceptions.isEmpty()) {
      return ProcessorResponse.ok();
    } else {
      return ProcessorResponse.itemError(exceptions);
    }
  }

  protected abstract List<IndexRequest> itemToIndexRequests(Item item);
}
