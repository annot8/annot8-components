/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tika.processors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;

@ProcessesContent(FileContent.class)
@ProcessesContent(InputStreamContent.class)
@CreatesContent(Text.class)
public class TikaExtractor extends AbstractComponent implements Processor {

  public static final String CONTENT_SUFFIX = "_tika";

  public ProcessorResponse process(Item item) {
    // Process InputStream
    item.getContents(InputStreamContent.class)
        .forEach(c -> this.createText(item, c.getName(), c.getData()));

    // Process Files
    item.getContents(FileContent.class)
        .forEach(
            c -> {
              try {
                this.createText(item, c.getName(), new FileInputStream(c.getData()));
              } catch (IOException e) {
                this.log().error("Unable to read File Content {}", c.getName(), e);
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
          item.create(Text.class)
              .withData(textHandler.toString())
              .withName(originalContentName + CONTENT_SUFFIX);

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
      this.log().error("Unable to extract text from Content {}", originalContentName, e);
    }
  }
}
