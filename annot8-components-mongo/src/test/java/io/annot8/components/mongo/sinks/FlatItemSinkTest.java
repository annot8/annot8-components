/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.components.responses.ProcessorResponse.Status;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Text;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.testing.testimpl.TestAnnotationStore;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.TestProperties;

public class FlatItemSinkTest extends AbstractSinkTest {

  private MongoConnection connection;
  private MongoDatabase database;
  private MongoCollection itemStore;
  private MongoCollection contentStore;
  private MongoCollection annotationStore;

  @BeforeEach
  public void beforeEach() {
    connection = mock(MongoConnection.class);
    database = mock(MongoDatabase.class);
    itemStore = mock(MongoCollection.class);
    contentStore = mock(MongoCollection.class);
    annotationStore = mock(MongoCollection.class);

    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection(Mockito.eq("item"))).thenReturn(itemStore);
    when(database.getCollection(Mockito.eq("content"))).thenReturn(contentStore);
    when(database.getCollection(Mockito.eq("annotation"))).thenReturn(annotationStore);
  }

  @Test
  public void testStore() {
    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection(eq("item"))).thenReturn(itemStore);
    when(database.getCollection(eq("content"))).thenReturn(contentStore);
    when(database.getCollection(eq("annotation"))).thenReturn(annotationStore);

    FlatMongoSink store = new FlatMongoSink(connection);
    Item item = new TestItem();
    Content content = addContent(item, "test", "testing");
    Annotation ann1 = addAnnotation(content, "test", 0, 1);
    Annotation ann2 = addAnnotation(content, "test2", 1, 2);

    ProcessorResponse response = store.process(item);
    assertEquals(ProcessorResponse.Status.OK, response.getStatus());

    Mockito.verify(itemStore, times(1)).insertOne(any(Document.class));
    Mockito.verify(contentStore, times(1)).insertMany(anyList());
    Mockito.verify(annotationStore, times(1)).insertMany(anyList());
  }

  @Test
  public void testProcessNonSerializableData() {
    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection(eq("item"))).thenReturn(itemStore);
    when(database.getCollection(eq("content"))).thenReturn(contentStore);
    when(database.getCollection(eq("annotation"))).thenReturn(annotationStore);

    FlatMongoSink store = new FlatMongoSink(connection);

    TestItem item = new TestItem();
    Content content = mock(Content.class);
    when(content.getId()).thenReturn("test");
    when(content.getDescription()).thenReturn("test");
    when(content.getAnnotations()).thenReturn(new TestAnnotationStore(content));
    when(content.getData()).thenReturn(new NonSerializableTestData("test"));
    when(content.getProperties()).thenReturn(new TestProperties());
    doReturn(Text.class).when(content).getContentClass();
    item.setContent(Collections.singletonMap("content", content));

    ProcessorResponse response = store.process(item);
    assertEquals(Status.ITEM_ERROR, response.getStatus());
    Mockito.verify(itemStore, times(0)).insertOne(any(Document.class));
    Mockito.verify(annotationStore, times(0)).insertMany(Mockito.anyList());
    Mockito.verify(contentStore, times(0)).insertMany(Mockito.anyList());
  }

  @Test
  public void testProcessNoContent() {
    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection(eq("item"))).thenReturn(itemStore);
    when(database.getCollection(eq("content"))).thenReturn(contentStore);
    when(database.getCollection(eq("annotation"))).thenReturn(annotationStore);

    FlatMongoSink store = new FlatMongoSink(connection);

    TestItem item = new TestItem();
    ProcessorResponse processorResponse = store.process(item);
    assertEquals(Status.OK, processorResponse.getStatus());
    Mockito.verify(itemStore, times(1)).insertOne(any(Document.class));
    Mockito.verify(contentStore, times(0)).insertMany(any());
    Mockito.verify(annotationStore, times(0)).insertMany(any());
  }
}
