/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sources;

import java.time.Instant;

import org.bson.Document;

import com.mongodb.client.MongoCursor;

import io.annot8.components.mongo.AbstractMongoComponent;
import io.annot8.components.mongo.data.MongoDocument;
import io.annot8.components.mongo.resources.MongoConnection;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.components.Source;
import io.annot8.core.components.responses.SourceResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.data.ItemFactory;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.core.exceptions.UnsupportedContentException;

/**
 * Reads the contents of a Mongo collection into items.
 *
 * <p>Note that this source will only run the query once, and once it has exhausted those results it
 * will return SourceResponse.done().
 */
@CreatesContent(MongoDocument.class)
public class MongoSource extends AbstractMongoComponent implements Source {

  private MongoCursor<Document> cursor = null;

  @Override
  public void configure(Context context, MongoConnection connection) {
    cursor = connection.getCollection().find().iterator();
    // TODO: Use change streams (collection.watch()) to pick up new documents
  }

  @Override
  public SourceResponse read(ItemFactory itemFactory) {
    if (cursor == null) {
      log().warn("Cursor is null, has configure been called?");
      return SourceResponse.sourceError();
    }

    if (!cursor.hasNext()) {
      cursor.close();
      return SourceResponse.done();
    }

    Document doc = cursor.next();

    Item item = itemFactory.create();

    try {
      // TODO: Add source here, but how do we get that?
      item.getProperties()
          .set(PropertyKeys.PROPERTY_KEY_ACCESSEDAT, Instant.now().getEpochSecond());

      item.create(MongoDocument.class).withName("document").withData(doc).save();
    } catch (UnsupportedContentException | IncompleteException e) {
      log().warn("Couldn't create item", e);
      item.discard();
      return SourceResponse.sourceError();
    }

    return SourceResponse.ok();
  }

  @Override
  public void close() {
    if (cursor != null) {
      cursor.close();
    }

    super.close();
  }
}
