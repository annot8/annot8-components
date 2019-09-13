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

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;

// @ProcessesContent(FileContent.class)
// @ProcessesContent(InputStreamContent.class)
// @CreatesContent(Text.class)
public class TikaExtractor extends AbstractProcessor {

  public static final String CONTENT_SUFFIX = "_tika";

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
              .withDescription(originalContentName + CONTENT_SUFFIX);

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
