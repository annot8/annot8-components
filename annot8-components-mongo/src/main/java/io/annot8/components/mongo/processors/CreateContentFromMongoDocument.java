/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractContentProcessor;
import io.annot8.components.mongo.data.MongoDocument;
import org.bson.Document;

@ComponentName("Create Content from Mongo Document")
@ComponentDescription("Converts a Mongo Document into other content")
public class CreateContentFromMongoDocument extends AbstractProcessorDescriptor<CreateContentFromMongoDocument.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(MongoDocument.class)
        .withCreatesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractContentProcessor<MongoDocument> {

    public Processor() {
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
}