/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.annot8.common.data.content.Text;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.components.responses.ProcessorResponse.Status;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;
import io.annot8.testing.testimpl.TestAnnotationStore;
import io.annot8.testing.testimpl.TestContext;
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

    FlatMongoSink store = new FlatMongoSink();
    store.configure(new TestContext(), connection);
    Item item = new TestItem();
    Content content = addContent(item, "test", "testing");
    Annotation ann1 = addAnnotation(content, "test", 0, 1);
    Annotation ann2 = addAnnotation(content, "test2", 1, 2);

    ProcessorResponse response = store.process(item);
    assertEquals(ProcessorResponse.Status.OK, response.getStatus());

    Document expectedItem = getExpecetedItem(item.getId());
    Document expectedContent = getExpectedContent(content.getId(), item.getId());
    Document expectedAnn1 =
        getExpectedAnnotation(
            ann1.getId(), content.getId(), ann1.getType(), "t", 0, 1, item.getId());
    Document expectedAnn2 =
        getExpectedAnnotation(
            ann2.getId(), content.getId(), ann2.getType(), "e", 1, 2, item.getId());
    List<Document> expectedAnnotations = new ArrayList<>();
    expectedAnnotations.add(expectedAnn1);
    expectedAnnotations.add(expectedAnn2);
    DocumentListArgMatcher matchesDocs = new DocumentListArgMatcher(expectedAnnotations);

    Mockito.verify(itemStore, times(1)).insertOne(expectedItem);
    Mockito.verify(contentStore, times(1)).insertMany(Collections.singletonList(expectedContent));
    Mockito.verify(annotationStore, times(1)).insertMany(argThat(matchesDocs));
  }

  @Test
  public void testProcessNonSerializableData() {
    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection(eq("item"))).thenReturn(itemStore);
    when(database.getCollection(eq("content"))).thenReturn(contentStore);
    when(database.getCollection(eq("annotation"))).thenReturn(annotationStore);

    FlatMongoSink store = new FlatMongoSink();
    store.configure(new TestContext(), connection);

    TestItem item = new TestItem();
    Content content = mock(Content.class);
    when(content.getId()).thenReturn("test");
    when(content.getName()).thenReturn("test");
    when(content.getAnnotations()).thenReturn(new TestAnnotationStore());
    when(content.getData()).thenReturn(new NonSerializableTestData("test"));
    when(content.getProperties()).thenReturn(new TestProperties());
    doReturn(Text.class).when(content).getContentClass();
    item.setContent(Collections.singletonMap("content", content));

    ProcessorResponse response = store.process(item);
    assertEquals(Status.ITEM_ERROR, response.getStatus());
    Mockito.verify(itemStore, times(0)).insertOne(any());
    Mockito.verify(contentStore, times(0)).insertMany(any());
    Mockito.verify(annotationStore, times(0)).insertMany(any());
  }

  @Test
  public void testProcessNoContent() {
    when(connection.getDatabase()).thenReturn(database);
    when(database.getCollection(eq("item"))).thenReturn(itemStore);
    when(database.getCollection(eq("content"))).thenReturn(contentStore);
    when(database.getCollection(eq("annotation"))).thenReturn(annotationStore);

    FlatMongoSink store = new FlatMongoSink();
    store.configure(new TestContext(), connection);

    TestItem item = new TestItem();
    Document expectedItem = getExpecetedItem(item.getId());
    ProcessorResponse processorResponse = store.process(item);
    assertEquals(Status.OK, processorResponse.getStatus());
    Mockito.verify(itemStore, times(1)).insertOne(expectedItem);
    Mockito.verify(contentStore, times(0)).insertMany(any());
    Mockito.verify(annotationStore, times(0)).insertMany(any());
  }

  private Document getExpecetedItem(String itemId) {
    String json =
        "{"
            + "\"id\":\""
            + itemId
            + "\","
            + "\"parentId\":null,"
            + "\"properties\":{},"
            + "\"contents\":null"
            + "}";
    return Document.parse(json);
  }

  private Document getExpectedContent(String contentId, String itemId) {
    String json =
        "{"
            + "\"id\":\""
            + contentId
            + "\","
            + "\"itemId\":\""
            + itemId
            + "\""
            + "\"name\":\"test\","
            + "\"type\":\"Text\","
            + "\"data\":\"testing\","
            + "\"properties\":{},"
            + "\"annotations\":null"
            + "}";
    return Document.parse(json);
  }

  private Document getExpectedAnnotation(
      String annotationId,
      String contentId,
      String type,
      String data,
      int begin,
      int end,
      String itemId) {
    String json =
        "{"
            + "\"id\":\""
            + annotationId
            + "\","
            + "\"type\":\""
            + type
            + "\","
            + "\"properties\":{},"
            + "\"bounds\":{\"begin\":"
            + begin
            + ", \"end\":"
            + end
            + "},"
            + "\"data\":\""
            + data
            + "\","
            + "\"contentId\":\""
            + contentId
            + "\""
            + "\"itemId\":\""
            + itemId
            + "\""
            + "}";
    return Document.parse(json);
  }
}
