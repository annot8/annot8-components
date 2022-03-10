/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.core.bulk.OperationType;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.components.elasticsearch.ElasticsearchSettings;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

public abstract class AbstractElasticsearchSink extends AbstractProcessor {
  protected final ElasticsearchClient client;
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

    RestClient restClient = builder.build();

    ElasticsearchTransport transport =
        new RestClientTransport(restClient, new JacksonJsonpMapper());
    client = new ElasticsearchClient(transport);

    this.index = index;
    this.forceString = forceString;

    // Validate connection
    try {
      if (!client.ping().value())
        throw new BadConfigurationException(
            "Could not connect to Elasticsearch - ping returned false");

    } catch (IOException e) {
      throw new BadConfigurationException("Could not connect to Elasticsearch", e);
    }

    // Delete index
    try {
      if (deleteIndex && client.indices().exists(r -> r.index(index)).value()) {
        log().info("Deleting index {}", index);
        client.indices().delete(r -> r.index(index));
      }
    } catch (IOException e) {
      log().error("An exception occurred whilst deleting index {}", index, e);
    }

    // Create index
    try {
      if (client.indices().exists(r -> r.index(index)).value()) {
        log().warn("Index {} already exists - existing mapping will be used", index);
      } else {
        Optional<TypeMapping> mapping = getTypeMapping();
        if (mapping.isPresent()) {
          log().info("Creating index {} with mapping", index);
          client.indices().create(r -> r.index(index).mappings(mapping.get()));
        }
      }
    } catch (IOException e) {
      log().error("An exception occurred whilst creating index {}", index, e);
    }
  }

  @Override
  public ProcessorResponse process(Item item) {
    List<IndexOperation<?>> requests;
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

    BulkRequest bulkRequest =
        new BulkRequest.Builder()
            .operations(
                requests.stream()
                    .map(io -> new BulkOperation.Builder().index(io).build())
                    .collect(Collectors.toList()))
            .build();

    List<Exception> exceptions = new ArrayList<>();
    try {
      log()
          .debug(
              "Performing bulk request to index item {} ({} index requests)",
              item.getId(),
              requests.size());
      BulkResponse response = client.bulk(bulkRequest);

      for (BulkResponseItem bulkItemResponse : response.items()) {
        ErrorCause error = bulkItemResponse.error();
        if (error != null) {
          log()
              .error(
                  "Failed to create/update document {} in index {}: {}",
                  bulkItemResponse.id(),
                  bulkItemResponse.index(),
                  error.reason());
          exceptions.add(new ProcessingException(error.reason()));

          continue;
        }

        if (bulkItemResponse.operationType() == OperationType.Index
            || bulkItemResponse.operationType() == OperationType.Create) {
          log()
              .debug(
                  "New document {} indexed in index {}",
                  bulkItemResponse.id(),
                  bulkItemResponse.index());
        } else if (bulkItemResponse.operationType() == OperationType.Update) {
          log()
              .debug(
                  "Existing document {} updated in index {}",
                  bulkItemResponse.id(),
                  bulkItemResponse.index());
        } else {
          log()
              .error(
                  "Unexpected result returned whilst indexing document {} in index {}: {}",
                  bulkItemResponse.id(),
                  bulkItemResponse.index(),
                  bulkItemResponse.operationType().name());
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

  /**
   * Return a map of {@link Property} to use for the property part of the index mapping, or an empty
   * optional if no explicit mapping should be set.
   */
  protected Optional<Map<String, Property>> getMapping() {
    return Optional.empty();
  }

  /**
   * Provides a TypeMapping object based off the properties map provided by getMapping(), or an
   * empty optional if no explicit mapping should be set.
   *
   * <p>Override for finer grained control over the mapping
   */
  protected Optional<TypeMapping> getTypeMapping() {
    Optional<Map<String, Property>> mapping = getMapping();

    if (mapping.isEmpty()) return Optional.empty();

    return Optional.of(TypeMapping.of(t -> t.properties(mapping.get())));
  }

  protected abstract List<IndexOperation<?>> itemToIndexRequests(Item item);
}
