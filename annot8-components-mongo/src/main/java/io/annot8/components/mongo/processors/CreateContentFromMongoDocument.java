/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.processors;

import org.bson.Document;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractContentProcessor;
import io.annot8.components.mongo.data.MongoDocument;

// @CreatesContent(Text.class)
// @ProcessesContent(MongoDocument.class)
public class CreateContentFromMongoDocument extends AbstractContentProcessor<MongoDocument> {

  public CreateContentFromMongoDocument() {
    super(MongoDocument.class);
  }

  @Override
  public void process(MongoDocument d) {

    Document doc = d.getData();
    for (String key : doc.keySet()) {
      Object o = doc.get(key);
      if (o instanceof String) {
        d.getItem()
            .createContent(Text.class)
            .withDescription("From Mongo key " + key)
            .withData(o.toString())
            .save();
      }

      // TODO: Handle other types - e.g. nested objects, numbers, booleans, etc.
    }
  }
}
