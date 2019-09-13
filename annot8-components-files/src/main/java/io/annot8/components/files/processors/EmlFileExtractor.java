/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import java.io.*;
import java.util.List;
import java.util.function.Supplier;

import org.apache.james.mime4j.dom.*;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.CharStreams;

import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;

public class EmlFileExtractor extends AbstractProcessor {

  public static final String PROPERTY_PART_NAME = "name";

  @Override
  public ProcessorResponse process(Item item) {
    item.getContents(FileContent.class)
        .filter(f -> f.getData().getName().endsWith(".eml"))
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
                  // Multi body part - attachments should become children items, other bodies become
                  // new content
                  processMultipart(item, (Multipart) body, "body");
                } else {
                  log().warn("Unexpected body type {}", body.getClass().getName());
                }

                // Remove the original *.eml content to avoid reprocessing
                item.removeContent(f);
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

  private static Supplier<InputStream> createSupplier(InputStream inputStream) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    return () -> new ByteArrayInputStream(buffer.toByteArray());
  }

  //  @Override
  //  public Stream<ContentCapability> processesContent() {
  //    return Stream.of(new ContentCapability(FileContent.class));
  //  }
  //
  //  @Override
  //  public Stream<ContentCapability> createsContent() {
  //    return Stream.of(
  //            new ContentCapability(Text.class), new ContentCapability(InputStreamContent.class));
  //  }
}
