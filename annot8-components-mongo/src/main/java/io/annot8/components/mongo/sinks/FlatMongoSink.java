/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bson.Document;

@ComponentName("Mongo Sink (Flat)")
@ComponentDescription("Created a flat representation of an item and persist to Mongo")
@SettingsClass(MongoConnectionSettings.class)
public class FlatMongoSink
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
  public Processor create(Context context) {
    return new Processor(getSettings());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withProcessesAnnotations("*", Bounds.class)
        .build();
  }

  public static class Processor extends AbstractMongoSink {

    private static final String ITEM = "item";
    private static final String CONTENT = "content";
    private static final String ANNOTATION = "annotation";
    private MongoCollection<Document> itemCollection;
    private MongoCollection<Document> contentsCollection;
    private MongoCollection<Document> annotationsCollection;

    public Processor(MongoConnection connection) {
      super(connection);
    }

    public Processor(MongoConnectionSettings settings) {
      super(settings);
    }

    @Override
    public void storeItem(Item item) throws Annot8Exception {
      String parentId = null;
      if (item.getParent().isPresent()) {
        parentId = item.getParent().get();
      }
      ItemDto itemDto = new ItemDto(item.getId(), parentId, item.getProperties().getAll(), null);

      Collection<ContentDto> contents =
          item.getContents()
              .map(
                  c ->
                      new ContentDto(
                          c.getId(),
                          c.getDescription(),
                          c.getData(),
                          sanitiseKeys(c.getProperties()),
                          null,
                          item.getId(),
                          c.getContentClass().getSimpleName()))
              .collect(Collectors.toList());

      Collection<AnnotationDto> annotations =
          item.getContents()
              .flatMap(c -> this.getAnnotations(c, item))
              .collect(Collectors.toList());

      Document itemDocument = null;
      List<Document> contentDocuments = null;
      List<Document> annotationDocuments = null;
      try {
        itemDocument = toMongoDocument(itemDto);
        contentDocuments = new ArrayList<>();
        annotationDocuments = new ArrayList<>();
        for (ContentDto content : contents) {
          contentDocuments.add(toMongoDocument(content));
        }
        for (AnnotationDto annotation : annotations) {
          annotationDocuments.add(toMongoDocument(annotation));
        }
      } catch (JsonProcessingException e) {
        log().error("Error converting object to document", e);
        throw new Annot8Exception("Error storing item", e);
      }

      itemCollection.insertOne(itemDocument);
      if (!contentDocuments.isEmpty()) {
        contentsCollection.insertMany(contentDocuments);
      }
      if (!annotationDocuments.isEmpty()) {
        annotationsCollection.insertMany(annotationDocuments);
      }
    }

    private Stream<AnnotationDto> getAnnotations(Content content, Item item) {
      return content.getAnnotations().getAll().map(a -> getAnnotation(a, content, item));
    }

    private AnnotationDto getAnnotation(Annotation annotation, Content content, Item item) {
      Object data = null;
      Optional optionalData = annotation.getBounds().getData(content, Object.class);
      if (optionalData.isPresent()) {
        data = optionalData.get();
      }

      return new AnnotationDto(
          annotation.getId(),
          annotation.getType(),
          annotation.getBounds(),
          data,
          sanitiseKeys(annotation.getProperties()),
          content.getId(),
          item.getId());
    }

    @Override
    protected void configureMongo(MongoConnection connection) {
      MongoDatabase database = connection.getDatabase();
      itemCollection = database.getCollection(ITEM);
      contentsCollection = database.getCollection(CONTENT);
      annotationsCollection = database.getCollection(ANNOTATION);

      itemCollection.createIndex(new Document("id", 1));
      contentsCollection.createIndex(new Document("id", 1));
      contentsCollection.createIndex(new Document("itemId", 1));
      annotationsCollection.createIndex(new Document("type", 1));
      annotationsCollection.createIndex(new Document("id", 1));
      annotationsCollection.createIndex(new Document("type", 1));
      annotationsCollection.createIndex(new Document("itemId", 1));
    }
  }
}
