/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mongodb.client.MongoCollection;

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

public class NestedItemSinkTest extends AbstractSinkTest {

  private MongoConnection connection;
  private MongoCollection collection;

  @BeforeEach
  public void beforeEach() {
    connection = mock(MongoConnection.class);
    collection = mock(MongoCollection.class);
    when(connection.getCollection()).thenReturn(collection);
  }

  @Test
  public void testStore() {
    NestedItemSink store = new NestedItemSink();
    store.configure(new TestContext(), connection);

    Item item = new TestItem();
    Content content = addContent(item, "test", "testing");
    Annotation annotation = addAnnotation(content, "test", 0, 1);

    ProcessorResponse response = store.process(item);
    assertEquals(Status.OK, response.getStatus());
    String expected = getExpected(item.getId(), content.getId(), annotation.getId());
    Document expectedDoc = Document.parse(expected);
    Mockito.verify(collection, Mockito.times(1)).insertOne(Mockito.eq(expectedDoc));
  }

  @Test
  public void testStoreNonSerializableItem() {
    NestedItemSink store = new NestedItemSink();
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

    ProcessorResponse processResponse = store.process(item);
    assertEquals(Status.ITEM_ERROR, processResponse.getStatus());
    Mockito.verify(collection, times(0)).insertOne(Mockito.any());
  }

  private String getExpected(String itemId, String contentId, String annotationId) {
    return "{"
        + "\"id\":\""
        + itemId
        + "\","
        + "\"parentId\":null,"
        + "\"properties\":{},"
        + "\"contents\":["
        + "{"
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
        + "\"annotations\":["
        + "{"
        + "\"id\":\""
        + annotationId
        + "\","
        + "\"type\":\"test\","
        + "\"properties\":{},"
        + "\"bounds\":{\"begin\":0, \"end\":1},"
        + "\"data\":\"t\","
        + "\"contentId\":\""
        + contentId
        + "\","
        + "\"itemId\":\""
        + itemId
        + "\"}]}]}";
  }
}
