/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCollection;
import io.annot8.api.annotations.Annotation;
import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.ProcessorDescriptor;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.components.mongo.data.AnnotationDto;
import io.annot8.components.mongo.data.ContentDto;
import io.annot8.components.mongo.data.ItemDto;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.components.mongo.resources.MongoConnectionSettings;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bson.Document;

@ComponentName("Mongo Sink (Nested)")
@ComponentDescription("Created a nested representation of an item and persist to Mongo")
@SettingsClass(MongoConnectionSettings.class)
public class NestedItemSink
    implements ProcessorDescriptor<FlatMongoSink.Processor, MongoConnectionSettings> {

  private String name;
  private MongoConnectionSettings settings;

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setSettings(MongoConnectionSettings settings) {
    this.settings = settings;
  }

  @Override
  public MongoConnectionSettings getSettings() {
    return settings;
  }

  @Override
  public FlatMongoSink.Processor create(Context context) {
    return new FlatMongoSink.Processor(getSettings());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withProcessesAnnotations("*", Bounds.class)
        .build();
  }

  public static class Processor extends AbstractMongoSink {

    private MongoCollection<Document> itemCollection;

    public Processor(MongoConnection connection) {
      super(connection);
    }

    public Processor(MongoConnectionSettings settings) {
      super(settings);
    }

    @Override
    public void storeItem(Item item) throws Annot8Exception {
      ItemDto itemDto = toDto(item);
      try {
        itemCollection.insertOne(toMongoDocument(itemDto));
      } catch (JsonProcessingException e) {
        log().error("Error converting item to mongo document", e);
        throw new Annot8Exception("Error storing item", e);
      }
    }

    @Override
    protected void configureMongo(MongoConnection connection) {
      itemCollection = connection.getCollection();

      itemCollection.createIndex(new Document("id", 1));
    }

    private ItemDto toDto(Item item) {
      String parentId = item.getParent().orElse(null);
      return new ItemDto(item.getId(), parentId, item.getProperties().getAll(), getContents(item));
    }

    private Collection<ContentDto> getContents(Item item) {
      return item.getContents().map(c -> toDto(c, item.getId())).collect(Collectors.toList());
    }

    private Collection<AnnotationDto> getAnnotations(Content content, String itemId) {
      return content
          .getAnnotations()
          .getAll()
          .map((a) -> toDto(a, content, itemId))
          .collect(Collectors.toList());
    }

    private ContentDto toDto(Content content, String itemId) {
      return new ContentDto(
          content.getId(),
          content.getDescription(),
          content.getData(),
          sanitiseKeys(content.getProperties()),
          getAnnotations(content, itemId),
          itemId,
          content.getContentClass().getSimpleName());
    }

    private AnnotationDto toDto(Annotation annotation, Content content, String itemId) {
      Object data = null;
      Optional<Object> optional = annotation.getBounds().getData(content);
      if (optional.isPresent()) {
        data = optional.get();
      }
      return new AnnotationDto(
          annotation.getId(),
          annotation.getType(),
          annotation.getBounds(),
          data,
          sanitiseKeys(annotation.getProperties()),
          content.getId(),
          itemId);
    }
  }
}
