/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.easyocr.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class AbstractEasyOCRProcessor extends AbstractProcessor {

  public static final String VERSION = "EasyOCR";

  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  private final int delay;
  private final int retries;

  public static Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(FileContent.class)
        .withProcessesContent(Image.class)
        .withCreatesContent(Text.class)
        .build();
  }

  protected AbstractEasyOCRProcessor() {
    this(1000, 10);
  }

  protected AbstractEasyOCRProcessor(int delay, int retries) {
    this.delay = delay;
    this.retries = retries;
    objectMapper = new ObjectMapper();
    httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
  }

  protected abstract String getUrl(String string);

  protected void tryInitializeEasyOCR(InitSettings initSettings) {
    try {
      int count = 0;
      while (true) {
        if (initializeEasyOCR(initSettings)) {
          break;
        } else {
          if (++count == retries) {
            throw new BadConfigurationException("Error initializing Easy OCR");
          }
          Thread.sleep(delay);
        }
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new BadConfigurationException("Error Initializing Easy OCR", ie);
    }
  }

  protected boolean initializeEasyOCR(InitSettings initSettings) {
    try {
      HttpResponse<String> response = postJSON(getUrl("/init"), initSettings);
      if (response.statusCode() != 200) {
        throw new IOException("Failed to initialise EasyOCR server {}" + response.body());
      } else {
        if (log().isInfoEnabled()) {
          log().info("Easy-OCR initialized: {}", response.statusCode());
        }
        return true;
      }
    } catch (IOException e) {
      log().debug("Error attempting to initialize EasyOCR", e);
      return false;
    }
  }

  private HttpResponse<String> postJSON(String uri, Object data) throws IOException {
    try {
      String requestBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
      log().trace("Sending request to {} with body {}", uri, requestBody);
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(uri))
              .timeout(Duration.ofMinutes(5))
              .header("Content-Type", "application/json")
              .POST(BodyPublishers.ofString(requestBody))
              .build();
      return httpClient.send(request, BodyHandlers.ofString());
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new IOException(ie);
    }
  }

  private Text createTextContent(Item item, String textContent, Content<?> sourceContent) {
    if (textContent == null || textContent.isBlank()) return null;

    return item.createContent(Text.class)
        .withDescription("OCR from " + sourceContent.getId())
        .withData(textContent)
        .withProperties(sourceContent.getProperties())
        .withProperty(PropertyKeys.PROPERTY_KEY_VERSION, VERSION)
        .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, sourceContent.getId())
        .save();
  }

  @Override
  public ProcessorResponse process(Item item) {
    List<Exception> exceptions = new LinkedList<>();

    item.getContents(FileContent.class)
        .forEach(
            c -> {
              try {
                processFile(item, c);
              } catch (Exception e) {
                log().error(e.getLocalizedMessage(), e);
                exceptions.add(e);
              }
            });

    item.getContents(Image.class)
        .forEach(
            c -> {
              try {
                processImage(item, c);
              } catch (Exception e) {
                log().error(e.getLocalizedMessage(), e);
                exceptions.add(e);
              }
            });

    return exceptions.isEmpty() ? ProcessorResponse.ok() : ProcessorResponse.itemError(exceptions);
  }

  private String processContent(String id, BodyPublisher publisher, String boundary) {
    try {
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(getUrl("/ocr")))
              .timeout(Duration.ofMinutes(2))
              .header("Content-Type", "multipart/form-data;boundary=" + boundary)
              .POST(publisher)
              .build();
      HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new ProcessingException(
            String.format(
                "Failed to run OCR on %s: %s - %s", id, response.statusCode(), response.body()));
      }

      return response.body();
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new ProcessingException(String.format("Failed to run OCR on %s", id), ie);
    } catch (IOException ioe) {
      throw new ProcessingException(String.format("Failed to run OCR on %s", id), ioe);
    }
  }

  @SuppressWarnings("java:S2245")
  private String newBoundary() {
    return new BigInteger(256, new Random()).toString();
  }

  private void processFile(Item item, FileContent c) throws IOException {
    log().debug("Running EasyOCR on {}", c.getId());

    String boundary = newBoundary();
    String ocr = processContent(c.getId(), fromPath(c.getData().toPath(), boundary), boundary);
    log().trace("OCR for item: {}\n{}", item.getId(), ocr);
    createTextContent(item, ocr, c);
  }

  private void processImage(Item item, Image c) throws IOException {
    log().debug("Running EasyOCR on {}", c.getId());

    String boundary = newBoundary();
    String ocr = processContent(c.getId(), fromImage(c, boundary), boundary);
    log().trace("OCR for item: {}\n{}", item.getId(), ocr);
    createTextContent(item, ocr, c);
  }

  private static BodyPublisher fromPath(Path path, String boundary) throws IOException {
    String mimeType = Files.probeContentType(path);
    String filename = path.getFileName().toString();
    byte[] data = Files.readAllBytes(path);

    return bodyPublisher(filename, mimeType, data, boundary);
  }

  private static BodyPublisher fromImage(Image image, String boundary) throws IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    image.saveAsPng(baos);
    byte[] data = baos.toByteArray();
    String mimeType = "image/png";

    return bodyPublisher(image.getId() + "-ocr.png", mimeType, data, boundary);
  }

  private static BodyPublisher bodyPublisher(
      String filename, String mimeType, byte[] data, String boundary) {

    byte[] separator =
        ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
            .getBytes(StandardCharsets.UTF_8);

    var byteArrays = new ArrayList<byte[]>();
    byteArrays.add(separator);
    byteArrays.add(
        ("\"file\"; filename=\"" + filename + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n")
            .getBytes(StandardCharsets.UTF_8));
    byteArrays.add(data);
    byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));

    byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
    return BodyPublishers.ofByteArrays(byteArrays);
  }
}
