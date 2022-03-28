/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.components.responses.ProcessorResponse.Status;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.components.mongo.utils.MongoConnection;
import io.annot8.testing.testimpl.TestAnnotationStore;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.TestProperties;
import java.util.Collections;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"unchecked", "rawtypes"})
public class FlatItemSinkTest extends AbstractSinkTest {

  private MongoConnection connection;
  private MongoDatabase database;
  private MongoCollection<Document> itemStore;
  private MongoCollection<Document> contentStore;
  private MongoCollection<Document> annotationStore;

  @BeforeEach
  public void beforeEach() {
    connection = mock(MongoConnection.class);
    database = mock(MongoDatabase.class);
    itemStore = mock(MongoCollection.class);
    contentStore = mock(MongoCollection.class);
    annotationStore = mock(MongoCollection.class);

    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection("item")).thenReturn((MongoCollection) itemStore);
    when(database.getCollection("content")).thenReturn((MongoCollection) contentStore);
    when(database.getCollection("annotation")).thenReturn((MongoCollection) annotationStore);
  }

  @Test
  public void testStore() {
    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection("item")).thenReturn((MongoCollection) itemStore);
    when(database.getCollection("content")).thenReturn((MongoCollection) contentStore);
    when(database.getCollection("annotation")).thenReturn((MongoCollection) annotationStore);

    try (Processor store = new FlatMongoSink.Processor(connection)) {
      Item item = new TestItem();
      Content<String> content = addContent(item, "test", "testing");
      addAnnotation(content, "test", 0, 1);
      addAnnotation(content, "test2", 1, 2);

      ProcessorResponse response = store.process(item);
      assertEquals(ProcessorResponse.Status.OK, response.getStatus());

      verify(itemStore, times(1)).insertOne(any(Document.class));
      verify(contentStore, times(1)).insertMany(anyList());
      verify(annotationStore, times(1)).insertMany(anyList());
    }
  }

  @Test
  public void testProcessNonSerializableData() {
    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection("item")).thenReturn(itemStore);
    when(database.getCollection("content")).thenReturn(contentStore);
    when(database.getCollection("annotation")).thenReturn(annotationStore);

    try (Processor store = new FlatMongoSink.Processor(connection)) {

      TestItem item = new TestItem();
      Content<NonSerializableTestData> content = mock(Content.class);
      when(content.getId()).thenReturn("test");
      when(content.getDescription()).thenReturn("test");
      when(content.getAnnotations()).thenReturn(new TestAnnotationStore(content));
      when(content.getData()).thenReturn(new NonSerializableTestData("test"));
      when(content.getProperties()).thenReturn(new TestProperties());
      doReturn(Text.class).when(content).getContentClass();
      item.setContent(Collections.singletonMap("content", content));

      ProcessorResponse response = store.process(item);
      assertEquals(Status.ITEM_ERROR, response.getStatus());
      verify(itemStore, times(0)).insertOne(any(Document.class));
      verify(annotationStore, times(0)).insertMany(anyList());
      verify(contentStore, times(0)).insertMany(anyList());
    }
  }

  @Test
  public void testProcessNoContent() {
    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection("item")).thenReturn(itemStore);
    when(database.getCollection("content")).thenReturn(contentStore);
    when(database.getCollection("annotation")).thenReturn(annotationStore);

    try (Processor store = new FlatMongoSink.Processor(connection)) {

      TestItem item = new TestItem();
      ProcessorResponse processorResponse = store.process(item);
      assertEquals(Status.OK, processorResponse.getStatus());
      verify(itemStore, times(1)).insertOne(any(Document.class));
      verify(contentStore, times(0)).insertMany(any());
      verify(annotationStore, times(0)).insertMany(any());
    }
  }
}
