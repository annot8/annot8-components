/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tika.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ToXMLContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

@ComponentName("Convert to HTML")
@ComponentDescription("Uses Apache Tika to convert documents into a HTML representation")
@SettingsClass(ConvertToHtml.Settings.class)
public class ConvertToHtml
    extends AbstractProcessorDescriptor<ConvertToHtml.Processor, ConvertToHtml.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.isRemoveSourceContent());
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(InputStreamContent.class)
            .withProcessesContent(FileContent.class)
            .withCreatesContent(InputStreamContent.class);

    if (getSettings().isRemoveSourceContent()) {
      builder =
          builder
              .withDeletesContent(InputStreamContent.class)
              .withDeletesContent(FileContent.class);
    }

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    private final boolean removeSourceContent;

    public Processor(boolean removeSourceContent) {
      this.removeSourceContent = removeSourceContent;
    }

    public ProcessorResponse process(Item item) {
      // Process InputStream
      item.getContents(InputStreamContent.class)
          .forEach(
              c -> {
                this.createHtml(item, c.getId(), c.getData());
                if (removeSourceContent) item.removeContent(c);
              });

      // Process Files
      item.getContents(FileContent.class)
          .forEach(
              c -> {
                try {
                  this.createHtml(item, c.getId(), new FileInputStream(c.getData()));
                  if (removeSourceContent) item.removeContent(c);
                } catch (IOException e) {
                  this.log().error("Unable to read File Content {}", c.getId(), e);
                }
              });

      return ProcessorResponse.ok();
    }

    private void createHtml(Item item, String originalContentName, InputStream inputStream) {
      try {
        // Create Tika context
        ContentHandler htmlHandler = new ToXMLContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        // Parse document
        AutoDetectParser parser = new AutoDetectParser();
        parser.parse(inputStream, htmlHandler, metadata, context);

        // Create Text content
        Content.Builder<InputStreamContent, InputStream> builder =
            item.createContent(InputStreamContent.class)
                .withData(
                    () ->
                        new ByteArrayInputStream(
                            htmlHandler.toString().getBytes(StandardCharsets.UTF_8)))
                .withDescription("HTML version of " + originalContentName);

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
        log().error("Unable to convert Content {} to HTML", originalContentName, e);
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private boolean removeSourceContent = true;

    @Override
    public boolean validate() {
      return true;
    }

    @Description(
        value = "Should the source Content be removed after successful processing?",
        defaultValue = "true")
    public boolean isRemoveSourceContent() {
      return removeSourceContent;
    }

    public void setRemoveSourceContent(boolean removeSourceContent) {
      this.removeSourceContent = removeSourceContent;
    }
  }
}
