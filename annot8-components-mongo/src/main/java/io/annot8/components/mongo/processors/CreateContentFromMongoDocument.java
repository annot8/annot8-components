/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.processors;

import org.bson.Document;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.components.mongo.data.MongoDocument;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;

@CreatesContent(Text.class)
@ProcessesContent(MongoDocument.class)
public class CreateContentFromMongoDocument extends AbstractComponent implements Processor {

  @Override
  public ProcessorResponse process(Item item) {
    item.getContents(MongoDocument.class)
        .forEach(
            d -> {
              Document doc = d.getData();
              for (String key : doc.keySet()) {
                Object o = doc.get(key);
                if (o instanceof String) {
                  item.create(Text.class).withName(key).withData(o.toString()).save();
                }

                // TODO: Handle other types - e.g. nested objects, numbers, booleans, etc.
              }
            });

    return ProcessorResponse.ok();
  }
}
