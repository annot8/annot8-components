/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.bson.Document;
import org.junit.jupiter.api.Test;

import io.annot8.components.mongo.data.MongoDocument.BuilderFactory;
import io.annot8.core.data.Content.Builder;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.testing.testimpl.TestAnnotationStore;
import io.annot8.testing.testimpl.TestItem;

public class MongoDocumentTest {

  @Test
  public void testBuilderFactory() {
    BuilderFactory factory =
        new MongoDocument.BuilderFactory(c -> new TestAnnotationStore(c.getId()));
    Builder<MongoDocument, Document> mongoDocumentBuilder = factory.create(new TestItem());

    assertNotNull(mongoDocumentBuilder);
  }

  @Test
  public void testBuilder() {
    BuilderFactory factory =
        new MongoDocument.BuilderFactory(c -> new TestAnnotationStore(c.getId()));
    Builder<MongoDocument, Document> mongoDocumentBuilder = factory.create(new TestItem());

    Document document = Document.parse("{}");

    MongoDocument content = null;
    try {
      content = mongoDocumentBuilder.withName("name").withData(document).save();
    } catch (IncompleteException e) {
      fail("Document builder should not be incomplete", e);
    }

    assertNotNull(content);
    assertEquals(document, content.getData());
    assertEquals(TestAnnotationStore.class, content.getAnnotations().getClass());
    assertEquals(MongoDocument.class, content.getContentClass());
    assertEquals(Document.class, content.getDataClass());
    assertEquals("name", content.getName());
    assertNotNull(content.getProperties());
    assertTrue(content.getProperties().getAll().isEmpty());
  }
}
