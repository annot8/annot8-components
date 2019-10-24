/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.properties.ImmutableProperties;
import io.annot8.components.mongo.AbstractMongoComponent;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.components.mongo.resources.MongoConnectionSettings;
import java.util.HashMap;
import java.util.Map;
import org.bson.Document;

public abstract class AbstractMongoSink extends AbstractMongoComponent implements Processor {

  private ObjectMapper mapper = new ObjectMapper();

  AbstractMongoSink(MongoConnectionSettings settings) {
    super(settings);

    configureMongo(getConnection());
  }

  public AbstractMongoSink(MongoConnection connection) {
    super(connection);
    configureMongo(getConnection());
  }

  protected abstract void storeItem(Item item) throws Annot8Exception;

  protected abstract void configureMongo(MongoConnection connection);

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
