/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.sources;

import com.mongodb.client.MongoCursor;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.SourceDescriptor;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.SourceResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.data.ItemFactory;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.exceptions.UnsupportedContentException;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.components.mongo.AbstractMongoComponent;
import io.annot8.components.mongo.data.MongoDocument;
import io.annot8.components.mongo.resources.MongoConnectionSettings;
import io.annot8.conventions.PropertyKeys;
import org.bson.Document;

import java.time.Instant;

/**
 * Reads the contents of a Mongo collection into items.
 *
 * <p>Note that this source will only run the query once, and once it has exhausted those results it
 * will return SourceResponse.done().
 */
@ComponentName("Mongo Source")
@ComponentDescription("Reads the contents of a Mongo collection into items")
@SettingsClass(MongoConnectionSettings.class)
public class MongoSource implements SourceDescriptor<MongoSource.Source, MongoConnectionSettings> {

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
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesContent(MongoDocument.class)
        .build();
  }

  @Override
  public Source create(Context context) {
    return new Source(getSettings());
  }

  public static class Source extends AbstractMongoComponent implements io.annot8.api.components.Source {

    private MongoCursor<Document> cursor = null;

    public Source(MongoConnectionSettings settings) {
      super(settings);
    }

    @Override
    public SourceResponse read(ItemFactory itemFactory) {
      if (cursor == null) {
        cursor = getConnection().getCollection().find().iterator();
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

        item.createContent(MongoDocument.class)
            .withDescription("Mongo document")
            .withData(doc)
            .save();
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
}