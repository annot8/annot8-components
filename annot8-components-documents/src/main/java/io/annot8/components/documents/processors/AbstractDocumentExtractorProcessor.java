/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import io.annot8.components.documents.data.ExtractionWithProperties;
import io.annot8.conventions.PropertyKeys;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for DocumentExtractor processors, handling a lot of the common boilerplate code.
 *
 * @param <T> The document type
 */
public abstract class AbstractDocumentExtractorProcessor<T, S extends DocumentExtractorSettings>
    extends AbstractProcessor {
  protected final S settings;

  protected static final String METADATA_SEPARATOR = "/";

  protected AbstractDocumentExtractorProcessor(Context context, S settings) {
    this.settings = settings;

    if (!isMetadataSupported() && settings.isExtractMetadata()) {
      log().warn("This extractor does not support extraction of metadata");
      this.settings.setExtractMetadata(false);
    }
    if (!isTextSupported() && settings.isExtractText()) {
      log().warn("This extractor does not support extraction of text");
      this.settings.setExtractText(false);
    }
    if (!isImagesSupported() && settings.isExtractImages()) {
      log().warn("This extractor does not support extraction of images");
      this.settings.setExtractImages(false);
    }
    if (!isTablesSupported() && settings.isExtractTables()) {
      log().warn("This extractor does not support extraction of tables");
      this.settings.setExtractTables(false);
    }
  }

  @Override
  public ProcessorResponse process(Item item) {
    reset();

    List<Exception> exceptions = new ArrayList<>();

    processFileContent(item, exceptions);
    processInputStreamContent(item, exceptions);

    if (exceptions.isEmpty()) {
      return ProcessorResponse.ok();
    } else {
      return ProcessorResponse.processingError(exceptions);
    }
  }

  private void processInputStreamContent(Item item, List<Exception> exceptions) {
    item.getContents(InputStreamContent.class)
        .filter(this::acceptInputStream)
        .forEach(
            c -> {
              log().info("Extracting content from InputStream Content {}", c.getId());

              T doc;
              try {
                doc = extractDocument(c);
              } catch (Exception e) {
                exceptions.add(e);
                return;
              }

              exceptions.addAll(extract(item, c.getId(), doc));

              tryClose(doc);

              if (settings.isDiscardOriginal()) item.removeContent(c);
            });
  }

  private void processFileContent(Item item, List<Exception> exceptions) {
    item.getContents(FileContent.class)
        .filter(this::acceptFile)
        .forEach(
            c -> {
              log()
                  .info(
                      "Extracting content from File Content {} ({})",
                      c.getId(),
                      c.getData().getPath());
              T doc;
              try {
                doc = extractDocument(c);
              } catch (Exception e) {
                exceptions.add(e);
                return;
              }

              exceptions.addAll(extract(item, c.getId(), doc));

              tryClose(doc);

              if (settings.isDiscardOriginal()) item.removeContent(c);
            });
  }

  private void tryClose(T doc) {
    if (doc instanceof Closeable) {
      try {
        ((Closeable) doc).close();
      } catch (IOException e) {
        // Do nothing
      }
    }
  }

  private List<Exception> extract(Item item, String contentId, T doc) {
    List<Exception> exceptions = new ArrayList<>();

    if (settings.isExtractMetadata()) {
      extractMetadata(item, doc, exceptions);
    }

    if (settings.isExtractText()) {
      extractText(item, contentId, doc, exceptions);
    }

    if (settings.isExtractImages()) {
      extractImages(item, contentId, doc, exceptions);
    }

    if (settings.isExtractTables()) {
      extractTables(item, contentId, doc, exceptions);
    }

    return exceptions;
  }

  private void extractMetadata(Item item, T doc, List<Exception> exceptions) {
    try {
      Map<String, Object> metadata = extractMetadata(doc);
      metadata.values().removeIf(Objects::isNull);
      metadata
          .values()
          .removeIf(
              o -> {
                if (o instanceof String) {
                  String s = (String) o;
                  return s.isEmpty();
                } else {
                  return false;
                }
              });

      metadata.forEach((k, v) -> item.getProperties().set(k, v));
    } catch (Exception e) {
      exceptions.add(e);
    }
  }

  private void extractText(Item item, String contentId, T doc, List<Exception> exceptions) {
    try {
      Collection<ExtractionWithProperties<String>> extractedText = extractText(doc);

      extractedText.stream()
          .filter(e -> !e.getExtractedValue().isEmpty())
          .forEach(
              e ->
                  item.createContent(Text.class)
                      .withDescription("Text extracted from " + contentId)
                      .withData(e.getExtractedValue())
                      .withProperties(new InMemoryProperties(e.getProperties()))
                      .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, contentId)
                      .save());
    } catch (Exception e) {
      exceptions.add(e);
    }
  }

  private void extractImages(Item item, String contentId, T doc, List<Exception> exceptions) {
    try {
      Collection<ExtractionWithProperties<BufferedImage>> extractedImages = extractImages(doc);

      for (ExtractionWithProperties<BufferedImage> e : extractedImages) {
        item.createContent(Image.class)
            .withDescription("Image extracted from " + contentId)
            .withData(e.getExtractedValue())
            .withProperties(new InMemoryProperties(e.getProperties()))
            .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, contentId)
            .save();
      }
    } catch (Exception e) {
      exceptions.add(e);
    }
  }

  private void extractTables(Item item, String contentId, T doc, List<Exception> exceptions) {
    try {
      Collection<ExtractionWithProperties<Table>> extractedTables = extractTables(doc);

      for (ExtractionWithProperties<Table> e : extractedTables) {
        item.createContent(TableContent.class)
            .withDescription("Table extracted from " + contentId)
            .withData(e.getExtractedValue())
            .withProperties(new InMemoryProperties(e.getProperties()))
            .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, contentId)
            .save();
      }
    } catch (Exception e) {
      exceptions.add(e);
    }
  }

  public void reset() {
    // Do nothing
  }

  // Abstract functions below here

  /** Returns true if this processor supports extracting metadata, and false otherwise */
  protected abstract boolean isMetadataSupported();

  /** Returns true if this processor supports extracting text, and false otherwise */
  protected abstract boolean isTextSupported();

  /** Returns true if this processor supports extracting images, and false otherwise */
  protected abstract boolean isImagesSupported();

  /** Returns true if this processor supports extracting tables, and false otherwise */
  protected abstract boolean isTablesSupported();

  /** Returns true if this processor should process the given file, and false otherwise */
  protected abstract boolean acceptFile(FileContent file);

  /**
   * Returns true if this processor should process the given InputStream, and false otherwise
   *
   * <p>Note that this method should not modify the InputStream
   */
  protected abstract boolean acceptInputStream(InputStreamContent inputStream);

  /** Convert the given file into the correct format for processing */
  protected abstract T extractDocument(FileContent file) throws IOException;

  /** Convert the given InputStream into the correct format for processing */
  protected abstract T extractDocument(InputStreamContent inputStreamContent) throws IOException;

  /**
   * Extract metadata from the document, returning it as a Map. Empty and null values in the
   * returned Map will be ignored.
   */
  protected abstract Map<String, Object> extractMetadata(T doc) throws ProcessingException;

  /** Extract text from the document */
  protected abstract Collection<ExtractionWithProperties<String>> extractText(T doc)
      throws ProcessingException;

  /** Extract images from the document */
  protected abstract Collection<ExtractionWithProperties<BufferedImage>> extractImages(T doc)
      throws ProcessingException;

  /** Extract tables from the document */
  protected abstract Collection<ExtractionWithProperties<Table>> extractTables(T doc)
      throws ProcessingException;

  // Utility functions below here
  protected static TemporalAccessor toTemporal(Date date) {
    if (date == null) return null;

    return date.toInstant();
  }

  protected static TemporalAccessor toTemporal(Calendar calendar) {
    if (calendar == null) return null;

    Instant instant = calendar.toInstant();

    if (calendar.getTimeZone() == null) {
      return instant;
    } else {
      return ZonedDateTime.ofInstant(instant, calendar.getTimeZone().toZoneId());
    }
  }

  protected static Map<String, Object> toMap(Metadata metadata) {
    Map<String, Object> properties = new HashMap<>();

    for (Directory directory : metadata.getDirectories()) {
      for (Tag tag : directory.getTags()) {
        String nameRoot = normaliseMetadataName(tag);

        properties.put(nameRoot, tag.getDescription());
        properties.put(
            nameRoot + METADATA_SEPARATOR + "raw", directory.getObject(tag.getTagType()));
      }
    }

    return properties;
  }

  protected static String normaliseMetadataName(Tag tag) {
    return camelCaseString(tag.getDirectoryName())
        + METADATA_SEPARATOR
        + camelCaseString(tag.getTagName());
  }

  private static String camelCaseString(String s) {
    StringBuilder sb = new StringBuilder();

    boolean spacePrecedes = false;
    for (char c : s.toLowerCase().toCharArray()) {
      if (c == ' ') {
        spacePrecedes = true;
      } else {
        if (spacePrecedes) {
          sb.append(String.valueOf(c).toUpperCase());
          spacePrecedes = false;
        } else {
          sb.append(c);
        }
      }
    }

    return sb.toString();
  }

  // TODO: Is there a better way than implementing our own?
  private static class InMemoryProperties implements io.annot8.api.properties.Properties {
    private Map<String, Object> map;

    public InMemoryProperties(Map<String, Object> map) {
      this.map = map;

      this.map.values().removeIf(Objects::isNull);
      this.map
          .values()
          .removeIf(
              o -> {
                if (o instanceof String) {
                  String s = (String) o;
                  return s.isEmpty();
                } else {
                  return false;
                }
              });
    }

    @Override
    public Map<String, Object> getAll() {
      return map;
    }
  }
}
