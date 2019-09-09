/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCollection;

import io.annot8.components.mongo.data.AnnotationDto;
import io.annot8.components.mongo.data.ContentDto;
import io.annot8.components.mongo.data.ItemDto;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;

public class NestedItemSink extends AbstractMongoSink {

  private MongoCollection<Document> itemCollection;

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
        content.getName(),
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
