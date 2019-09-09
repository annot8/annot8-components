/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Supplier;

import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.CharStreams;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Content.Builder;
import io.annot8.core.data.Item;

@ProcessesContent(FileContent.class)
@CreatesContent(Text.class)
@CreatesContent(InputStreamContent.class)
public class EmlFileExtractor extends AbstractComponent implements Processor {

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
                item.removeContent(f.getName());
              } catch (IOException e) {
                log()
                    .error(
                        "Could not read file {} in content {}",
                        f.getData().getName(),
                        f.getName(),
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

        Builder<Text, String> builder = item.create(Text.class).withData(text).withName(name);

        for (String key : headers.keySet()) {
          builder.withProperty(key, unlist(headers.get(key)));
        }

        builder.save();
      } else if (body instanceof BinaryBody) {
        BinaryBody binaryBody = (BinaryBody) body;

        Builder<InputStreamContent, InputStream> builder =
            item.create(InputStreamContent.class)
                .withData(createSupplier(binaryBody.getInputStream()))
                .withName(name);

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

      Builder<InputStreamContent, InputStream> builder =
          item.create()
              .create(InputStreamContent.class)
              .withData(createSupplier(inputStream))
              .withName(name);

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
}
