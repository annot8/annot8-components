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
import io.annot8.common.components.logging.Logging;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.components.documents.data.ExtractionWithProperties;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

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
import java.util.Optional;

/**
 * Base class for DocumentExtractor processors, handling a lot of the common boilerplate code.
 *
 * @param <T> The document type
 */
public abstract class AbstractDocumentExtractorProcessor<T> extends AbstractProcessor {
  private final Context context;
  private final DocumentExtractorSettings settings;

  protected static final String METADATA_SEPARATOR = "/";

  public AbstractDocumentExtractorProcessor(Context context, DocumentExtractorSettings settings) {
    this.context = context;
    this.settings = settings;

    Logger logger = getLogger();

    if (!isMetadataSupported() && settings.isExtractMetadata()) {
      logger.warn("This extractor does not support extraction of metadata");
      this.settings.setExtractMetadata(false);
    }
    if (!isTextSupported() && settings.isExtractText()) {
      logger.warn("This extractor does not support extraction of text");
      this.settings.setExtractText(false);
    }
    if (!isImagesSupported() && settings.isExtractImages()) {
      logger.warn("This extractor does not support extraction of images");
      this.settings.setExtractImages(false);
    }
  }

  @Override
  public ProcessorResponse process(Item item) {
    List<Exception> exceptions = new ArrayList<>();

    item.getContents(FileContent.class)
        .filter(this::acceptFile)
        .forEach(
            c -> {
              T doc;
              try {
                doc = extractDocument(c);
              } catch (Exception e) {
                exceptions.add(e);
                return;
              }

              exceptions.addAll(extract(item, c.getId(), doc));

              if(doc instanceof Closeable){
                try {
                  ((Closeable)doc).close();
                } catch (IOException e) {
                  //Do nothing
                }
              }

              if(settings.isDiscardOriginal())
                item.removeContent(c);
            });

    item.getContents(InputStreamContent.class)
        .filter(this::acceptInputStream)
        .forEach(
            c -> {
              T doc;
              try {
                doc = extractDocument(c);
              } catch (Exception e) {
                exceptions.add(e);
                return;
              }

              exceptions.addAll(extract(item, c.getId(), doc));

              if(doc instanceof Closeable){
                try {
                  ((Closeable)doc).close();
                } catch (IOException e) {
                  //Do nothing
                }
              }

              if(settings.isDiscardOriginal())
                item.removeContent(c);
            });

    if (exceptions.isEmpty()) {
      return ProcessorResponse.ok();
    } else {
      return ProcessorResponse.processingError(exceptions);
    }
  }

  private List<Exception> extract(Item item, String contentId, T doc) {
    List<Exception> exceptions = new ArrayList<>();

    if (settings.isExtractMetadata()) {
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

    if (settings.isExtractText()) {
      try {
        Collection<ExtractionWithProperties<String>> extractedText = extractText(doc);

        for (ExtractionWithProperties<String> e : extractedText) {
          item.createContent(Text.class)
              .withDescription("Text extracted from " + contentId)
              .withData(e.getExtractedValue())
              .withProperties(new InMemoryProperties(e.getProperties()))
              .save();
        }
      } catch (Exception e) {
        exceptions.add(e);
      }
    }

    if (settings.isExtractImages()) {
      try {
        Collection<ExtractionWithProperties<BufferedImage>> extractedImages = extractImages(doc);

        for (ExtractionWithProperties<BufferedImage> e : extractedImages) {
          item.createContent(Image.class)
              .withDescription("Image extracted from " + contentId)
              .withData(e.getExtractedValue())
              .withProperties(new InMemoryProperties(e.getProperties()))
              .save();
        }
      } catch (Exception e) {
        exceptions.add(e);
      }
    }

    return exceptions;
  }

  // Abstract functions below here

  /** Returns true if this processor supports extracting metadata, and false otherwise */
  public abstract boolean isMetadataSupported();
  /** Returns true if this processor supports extracting text, and false otherwise */
  public abstract boolean isTextSupported();
  /** Returns true if this processor supports extracting images, and false otherwise */
  public abstract boolean isImagesSupported();

  /** Returns true if this processor should process the given file, and false otherwise */
  public abstract boolean acceptFile(FileContent file);
  /**
   * Returns true if this processor should process the given InputStream, and false otherwise
   *
   * <p>Note that this method should not modify the InputStream
   */
  public abstract boolean acceptInputStream(InputStreamContent inputStream);

  /** Convert the given file into the correct format for processing */
  public abstract T extractDocument(FileContent file) throws IOException;
  /** Convert the given InputStream into the correct format for processing */
  public abstract T extractDocument(InputStreamContent inputStreamContent) throws IOException;

  /**
   * Extract metadata from the document, returning it as a Map. Empty and null values in the
   * returned Map will be ignored.
   */
  public abstract Map<String, Object> extractMetadata(T doc) throws ProcessingException;
  /** Extract text from the document */
  public abstract Collection<ExtractionWithProperties<String>> extractText(T doc)
      throws ProcessingException;
  /** Extract images from the document */
  public abstract Collection<ExtractionWithProperties<BufferedImage>> extractImages(T doc)
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

  protected final Logger getLogger() {
    Optional<Logging> logging = context.getResource(Logging.class);
    if (logging.isPresent()) {
      return logging.get().getLogger(this.getClass());
    } else {
      return NOPLogger.NOP_LOGGER;
    }
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
