/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.annot8.common.serialisation.jackson.Annot8ObjectMapperFactory;
import io.annot8.components.mongo.AbstractMongoComponent;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.components.mongo.resources.MongoFactory;
import io.annot8.core.capabilities.UsesResource;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.properties.ImmutableProperties;

@UsesResource(MongoFactory.class)
public abstract class AbstractMongoSink extends AbstractMongoComponent implements Processor {

  private ObjectMapper mapper;

  protected abstract void storeItem(Item item) throws Annot8Exception;

  protected abstract void configureMongo(MongoConnection connection);

  @Override
  protected void configure(Context context, MongoConnection connection) {
    mapper = new ObjectMapper();
    Annot8ObjectMapperFactory factory = new Annot8ObjectMapperFactory();
    factory.scan();
    factory.configure(mapper);
    this.configureMongo(connection);
  }

  @Override
  public ProcessorResponse process(Item item) {
    try {
      this.storeItem(item);
    } catch (Annot8Exception e) {
      log().error("Failed to store item", e);
      return ProcessorResponse.itemError();
    }
    return ProcessorResponse.ok();
  }

  protected Document toMongoDocument(Object object) throws JsonProcessingException {
    String json = mapper.writeValueAsString(object);
    return Document.parse(json);
  }

  protected Map<String, Object> sanitiseKeys(ImmutableProperties properties) {
    Map<String, Object> sanitisedProperties = new HashMap<>();
    properties.getAll().forEach((k, v) -> sanitisedProperties.put(k.replaceAll("\\.", "-"), v));
    return sanitisedProperties;
  }
}
