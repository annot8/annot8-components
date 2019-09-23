/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tika.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ComponentName("Tika Extractor")
@ComponentDescription("Extract text from files and streams using Apache Tika")
public class TikaExtractor extends AbstractProcessorDescriptor<TikaExtractor.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(InputStreamContent.class)
        .withProcessesContent(FileContent.class)
        .withCreatesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {

    public ProcessorResponse process(Item item) {
      // Process InputStream
      item.getContents(InputStreamContent.class)
          .forEach(c -> this.createText(item, c.getId(), c.getData()));

      // Process Files
      item.getContents(FileContent.class)
          .forEach(
              c -> {
                try {
                  this.createText(item, c.getId(), new FileInputStream(c.getData()));
                } catch (IOException e) {
                  this.log().error("Unable to read File Content {}", c.getId(), e);
                }
              });

      return ProcessorResponse.ok();
    }

    private void createText(Item item, String originalContentName, InputStream inputStream) {
      try {
        // Create Tika context
        BodyContentHandler textHandler = new BodyContentHandler(Integer.MAX_VALUE);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        // Parse document
        AutoDetectParser parser = new AutoDetectParser();
        parser.parse(inputStream, textHandler, metadata, context);

        // Create Text content
        Content.Builder<Text, String> builder =
            item.createContent(Text.class)
                .withData(textHandler.toString())
                .withDescription("Tika'd output of " + originalContentName);

        // Add metadata to content
        for (String name : metadata.names()) {
          if (metadata.isMultiValued(name)) {
            builder = builder.withProperty(name, metadata.getValues(name));
          } else {
            builder = builder.withProperty(name, metadata.get(name));
          }
        }

        // Save content
        builder.save();
      } catch (SAXException | IOException | TikaException e) {
        log().error("Unable to extract text from Content {}", originalContentName, e);
      }
    }
  }
}
