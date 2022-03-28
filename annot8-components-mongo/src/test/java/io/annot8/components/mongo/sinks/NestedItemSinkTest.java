/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mongodb.client.MongoCollection;
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
import org.mockito.Mockito;

public class NestedItemSinkTest extends AbstractSinkTest {

  private MongoConnection connection;
  private MongoCollection<Document> collection;

  @BeforeEach
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void beforeEach() {
    connection = mock(MongoConnection.class);
    collection = mock(MongoCollection.class);
    when(connection.getCollection()).thenReturn((MongoCollection) collection);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testStore() {
    try (Processor store = new NestedItemSink.Processor(connection)) {

      Mockito.reset(collection);

      Item item = new TestItem();
      Content<String> content = addContent(item, "test", "testing");
      addAnnotation(content, "test", 0, 1);

      ProcessorResponse response = store.process(item);
      assertEquals(Status.OK, response.getStatus());
      verify(collection, times(1)).insertOne(any(Document.class));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testStoreNonSerializableItem() {
    try (Processor store = new NestedItemSink.Processor(connection)) {

      TestItem item = new TestItem();
      Content<NonSerializableTestData> content = mock(Content.class);
      when(content.getId()).thenReturn("test");
      when(content.getDescription()).thenReturn("desc");
      when(content.getAnnotations()).thenReturn(new TestAnnotationStore(content));
      when(content.getData()).thenReturn(new NonSerializableTestData("test"));
      when(content.getProperties()).thenReturn(new TestProperties());
      doReturn(Text.class).when(content).getContentClass();
      item.setContent(Collections.singletonMap("content", content));

      ProcessorResponse processResponse = store.process(item);
      assertEquals(Status.ITEM_ERROR, processResponse.getStatus());
      verify(collection, times(0)).insertOne(any());
    }
  }
}
