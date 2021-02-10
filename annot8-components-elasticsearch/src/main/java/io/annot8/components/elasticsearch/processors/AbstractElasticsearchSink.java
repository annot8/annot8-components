/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.components.elasticsearch.ElasticsearchSettings;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;

public abstract class AbstractElasticsearchSink extends AbstractProcessor {
  protected final RestHighLevelClient client;
  protected final String index;
  protected final boolean forceString;

  public AbstractElasticsearchSink(ElasticsearchSettings settings) {
    this(
        List.of(settings.host()),
        settings.getIndex(),
        settings.isDeleteIndex(),
        settings.isForceString(),
        settings.credentials());
  }

  public AbstractElasticsearchSink(
      List<HttpHost> hosts,
      String index,
      boolean deleteIndex,
      boolean forceString,
      CredentialsProvider credentials) {

    RestClientBuilder builder = RestClient.builder(hosts.toArray(new HttpHost[0]));

    if (credentials != null) {
      builder.setHttpClientConfigCallback(
          httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentials));
    }

    client = new RestHighLevelClient(builder);

    this.index = index;
    this.forceString = forceString;

    // Validate connection
    try {
      if (!client.ping(RequestOptions.DEFAULT))
        throw new BadConfigurationException(
            "Could not connect to Elasticsearch - ping returned false");

    } catch (IOException e) {
      throw new BadConfigurationException("Could not connect to Elasticsearch", e);
    }

    // Delete index
    try {
      if (deleteIndex
          && client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT)) {
        log().info("Deleting index {}", index);
        client.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
      }
    } catch (IOException e) {
      log().error("An exception occurred whilst deleting index {}", index, e);
    }

    // Create index
    try {
      if (client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT)) {
        log().warn("Index {} already exists - existing mapping will be used", index);
      } else {
        Optional<Map<String, Object>> mapping = getMapping();
        if (mapping.isPresent()) {
          log().info("Creating index {} with mapping", index);
          client
              .indices()
              .create(new CreateIndexRequest(index).mapping(mapping.get()), RequestOptions.DEFAULT);
        }
      }
    } catch (IOException e) {
      log().error("An exception occurred whilst creating index {}", index, e);
    }
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
    List<IndexRequest> requests;
    try {
      requests = itemToIndexRequests(item);
    } catch (Exception e) {
      log().error("Unable to serialize item {}: {}", item.getId(), e.getMessage());
      return ProcessorResponse.itemError(e);
    }

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
    } catch (ConnectException e) {
      log()
          .error(
              "Unable to connect to Elasticsearch whilst performing bulk request: {}",
              e.getMessage());
      return ProcessorResponse.processingError(e);
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

  protected Optional<Map<String, Object>> getMapping() {
    return Optional.empty();
  }

  protected abstract List<IndexRequest> itemToIndexRequests(Item item);
}
