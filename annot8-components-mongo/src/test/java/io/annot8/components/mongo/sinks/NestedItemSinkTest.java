/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mongodb.client.MongoCollection;

import io.annot8.common.data.content.Text;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.components.responses.ProcessorResponse.Status;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.testing.testimpl.TestAnnotationStore;
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
    NestedItemSink store = new NestedItemSink(connection);
    Mockito.reset(collection);

    Item item = new TestItem();
    Content content = addContent(item, "test", "testing");
    Annotation annotation = addAnnotation(content, "test", 0, 1);

    ProcessorResponse response = store.process(item);
    assertEquals(Status.OK, response.getStatus());
    Mockito.verify(collection, Mockito.times(1)).insertOne(Mockito.any(Document.class));
  }

  @Test
  public void testStoreNonSerializableItem() {
    NestedItemSink store = new NestedItemSink(connection);

    TestItem item = new TestItem();
    Content content = mock(Content.class);
    when(content.getId()).thenReturn("test");
    when(content.getDescription()).thenReturn("desc");
    when(content.getAnnotations()).thenReturn(new TestAnnotationStore(content));
    when(content.getData()).thenReturn(new NonSerializableTestData("test"));
    when(content.getProperties()).thenReturn(new TestProperties());
    doReturn(Text.class).when(content).getContentClass();
    item.setContent(Collections.singletonMap("content", content));

    ProcessorResponse processResponse = store.process(item);
    assertEquals(Status.ITEM_ERROR, processResponse.getStatus());
    Mockito.verify(collection, times(0)).insertOne(Mockito.any());
  }
}
