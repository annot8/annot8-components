/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.CharStreams;
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
import io.annot8.common.data.content.Text;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.james.mime4j.dom.*;

@ComponentName("Eml File Extractor")
@ComponentDescription("Extract text and attachments from *.eml files and create new Content")
@SettingsClass(EmlFileExtractor.Settings.class)
public class EmlFileExtractor
    extends AbstractProcessorDescriptor<EmlFileExtractor.Processor, EmlFileExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.isRemoveSourceContent(), settings.getExtensions());
  }

  @Override
  public Capabilities capabilities() {

    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(FileContent.class)
            .withCreatesContent(Text.class)
            .withCreatesContent(InputStreamContent.class);

    if (getSettings().isRemoveSourceContent()) {
      builder = builder.withDeletesContent(FileContent.class);
    }

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    public static final String PROPERTY_PART_NAME = "name";

    private final boolean removeSourceContent;
    private final List<String> extensions;

    public Processor(boolean removeSourceContent, List<String> extensions) {
      this.removeSourceContent = removeSourceContent;
      this.extensions = extensions;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents(FileContent.class)
          .filter(
              f ->
                  extensions.isEmpty()
                      || extensions.contains(getExtension(f.getData().getName()).orElse("")))
          .forEach(
              f -> {
                try {
                  Message message = Message.Builder.of(new FileInputStream(f.getData())).build();

                  ListMultimap<String, String> headers = ArrayListMultimap.create();
                  message
                      .getHeader()
                      .getFields()
                      .forEach(field -> headers.put(field.getName(), field.getBody()));

                  for (String key : headers.keySet()) {
                    item.getProperties().set(key, unlist(headers.get(key)));
                  }

                  Body body = message.getBody();
                  if (body instanceof SingleBody) {
                    // Single body part, so create a new content
                    createContentFromBody(item, body, "body", ArrayListMultimap.create());
                  } else if (body instanceof Multipart) {
                    // Multi body part - attachments should become children items, other bodies
                    // become
                    // new content
                    processMultipart(item, (Multipart) body, "body");
                  } else {
                    log().warn("Unexpected body type {}", body.getClass().getName());
                  }

                  // If processed, then remove it our item so it doesn't get reprocessed
                  if (removeSourceContent) item.removeContent(f);
                } catch (IOException e) {
                  log()
                      .error(
                          "Could not read file {} in content {}",
                          f.getData().getName(),
                          f.getId(),
                          e);
                }
              });

      return ProcessorResponse
          .ok(); // TODO: If we weren't able to process successfully, should return an error!
    }

    private Optional<String> getExtension(String filename) {
      return Optional.ofNullable(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
    }

    private void processMultipart(Item item, Multipart multipart, String baseName) {
      int bodyCount = 0;
      for (Entity entity : multipart.getBodyParts()) {

        ListMultimap<String, String> headers = ArrayListMultimap.create();
        entity
            .getHeader()
            .getFields()
            .forEach(field -> headers.put(field.getName(), field.getBody()));

        if (entity.getFilename() != null) {
          // Attachment
          createItemFromBody(item, entity.getBody(), entity.getFilename(), headers);
        } else {
          // Message or nested multipart
          bodyCount++;
          createContentFromBody(item, entity.getBody(), baseName + "-" + bodyCount, headers);
        }
      }
    }

    private void createContentFromBody(
        Item item, Body body, String name, ListMultimap<String, String> headers) {
      log().debug("Creating content {} from body", name);
      try {
        if (body instanceof TextBody) {
          TextBody textBody = (TextBody) body;
          String text = CharStreams.toString(textBody.getReader());

          Content.Builder<Text, String> builder =
              item.createContent(Text.class)
                  .withData(text)
                  .withDescription("Email " + name)
                  .withProperty(PROPERTY_PART_NAME, name);

          for (String key : headers.keySet()) {
            builder.withProperty(key, unlist(headers.get(key)));
          }

          builder.save();
        } else if (body instanceof BinaryBody) {
          BinaryBody binaryBody = (BinaryBody) body;

          Content.Builder<InputStreamContent, InputStream> builder =
              item.createContent(InputStreamContent.class)
                  .withData(createSupplier(binaryBody.getInputStream()))
                  .withDescription("Email " + name)
                  .withProperty(PROPERTY_PART_NAME, name);

          for (String key : headers.keySet()) {
            builder.withProperty(key, unlist(headers.get(key)));
          }

          builder.save();
        } else if (body instanceof Multipart) {
          processMultipart(item, (Multipart) body, name);
        } else {
          log().warn("Unexpected body type {}", body.getClass().getName());
        }

      } catch (Exception e) {
        log().error("Unable to create content from body", e);
      }
    }

    private void createItemFromBody(
        Item item, Body body, String name, ListMultimap<String, String> headers) {
      log().debug("Creating item {} from body", name);
      try {
        InputStream inputStream;
        if (body instanceof SingleBody) {
          inputStream = ((SingleBody) body).getInputStream();
        } else {
          log().warn("Unexpected body type {}", body.getClass().getName());
          return;
        }

        Content.Builder<InputStreamContent, InputStream> builder =
            item.createChild()
                .createContent(InputStreamContent.class)
                .withData(createSupplier(inputStream))
                .withDescription("Email " + name)
                .withProperty(PROPERTY_PART_NAME, name);

        for (String key : headers.keySet()) {
          builder.withProperty(key, unlist(headers.get(key)));
        }

        builder.save();
      } catch (Exception e) {
        log().error("Unable to create new item from body", e);
      }
    }

    private static Object unlist(List<String> list) {
      if (list.size() == 1) {
        return list.get(0);
      }

      return list;
    }

    private static Supplier<InputStream> createSupplier(InputStream inputStream)
        throws IOException {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      int nRead;
      byte[] data = new byte[16384];

      while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }

      buffer.flush();

      return () -> new ByteArrayInputStream(buffer.toByteArray());
    }
  }

  public static class Settings extends RemoveSourceContentSettings {
    private List<String> extensions = List.of("eml");

    @Description(
        "The list of file extensions on which this processor will act (case insensitive). If empty, then the processor will act on all files.")
    public List<String> getExtensions() {
      return extensions;
    }

    public void setExtensions(List<String> extensions) {
      this.extensions = extensions.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public boolean validate() {
      return super.validate() && extensions != null;
    }
  }
}
