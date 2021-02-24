/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.components.documents.data.ExtractionWithProperties;
import io.annot8.conventions.PropertyKeys;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;

/** Extracts content from HTML files */
@ComponentName("HTML Extractor")
@ComponentDescription("Extracts image and text from HTML (*.html) files")
@ComponentTags({"documents", "html", "extractor", "text", "images", "metadata"})
@SettingsClass(DocumentExtractorSettings.class)
public class HtmlExtractor
    extends AbstractDocumentExtractorDescriptor<
        HtmlExtractor.Processor, DocumentExtractorSettings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<Document, DocumentExtractorSettings> {
    private final Logger logger = getLogger();

    public Processor(Context context, DocumentExtractorSettings settings) {
      super(context, settings);
    }

    @Override
    public boolean isMetadataSupported() {
      return true;
    }

    @Override
    public boolean isTextSupported() {
      return true;
    }

    @Override
    public boolean isImagesSupported() {
      return true;
    }

    @Override
    public boolean acceptFile(FileContent file) {
      return file.getData().getName().toLowerCase().endsWith(".htm")
          || file.getData().getName().toLowerCase().endsWith(".html");
    }

    @Override
    public boolean acceptInputStream(InputStreamContent inputStream) {
      BufferedInputStream bis = new BufferedInputStream(inputStream.getData());
      FileMagic fm;
      try {
        fm = FileMagic.valueOf(bis);
      } catch (IOException e) {
        return false;
      }

      return FileMagic.HTML == fm;
    }

    @Override
    public Document extractDocument(FileContent file) throws IOException {
      return Jsoup.parse(file.getData(), StandardCharsets.UTF_8.name());
    }

    @Override
    public Document extractDocument(InputStreamContent inputStreamContent) throws IOException {
      return Jsoup.parse(inputStreamContent.getData(), StandardCharsets.UTF_8.name(), "");
    }

    @Override
    public Map<String, Object> extractMetadata(Document doc) {
      Map<String, Object> metadata = new HashMap<>();

      metadata.put(PropertyKeys.PROPERTY_KEY_TITLE, doc.title());
      metadata.put(PropertyKeys.PROPERTY_KEY_LANGUAGE, doc.select("html").first().attr("lang"));

      doc.getElementsByTag("meta")
          .forEach(
              e -> {
                String name = e.attr("name");

                if (name.isBlank() && e.hasAttr("http-equiv")) {
                  name = "http" + METADATA_SEPARATOR + e.attr("http-equiv");
                }

                if (!name.isBlank()) {
                  String content = e.attr("content");

                  switch (name) {
                    case "application-name":
                      addOrAppend(metadata, DocumentProperties.APPLICATION, content);
                      break;
                    case "author":
                      addOrAppend(metadata, DocumentProperties.AUTHOR, content);
                      break;
                    case "creator":
                      addOrAppend(metadata, DocumentProperties.CREATOR, content);
                      break;
                    case "description":
                      addOrAppend(metadata, PropertyKeys.PROPERTY_KEY_DESCRIPTION, content);
                      break;
                    case "generator":
                      addOrAppend(metadata, DocumentProperties.GENERATOR, content);
                      break;
                    case "keywords":
                      List<String> keywords = Arrays.asList(content.split("\\s*,\\s*"));
                      addOrAppend(metadata, DocumentProperties.KEYWORDS, keywords);
                      break;
                    case "publisher":
                      addOrAppend(metadata, DocumentProperties.PUBLISHER, content);
                      break;
                    default:
                      addOrAppend(metadata, name, content);
                  }
                } else {
                  if (e.hasAttr("charset")) {
                    metadata.put("charset", e.attr("charset"));
                  }
                }
              });

      return metadata;
    }

    private void addOrAppend(Map<String, Object> map, String key, String value) {
      if (map.containsKey(key)) {
        Object existingValue = map.get(key);
        if (existingValue instanceof Collection) {
          Collection<Object> c = (Collection<Object>) existingValue;
          c.add(value);

          map.put(key, c);
        } else {
          List<Object> newList = new ArrayList<>();
          newList.add(existingValue);
          newList.add(value);

          map.put(key, newList);
        }
      } else {
        map.put(key, value);
      }
    }

    private void addOrAppend(Map<String, Object> map, String key, Collection<String> value) {
      if (map.containsKey(key)) {
        Object existingValue = map.get(key);
        if (existingValue instanceof Collection) {
          Collection<Object> c = (Collection<Object>) existingValue;
          c.addAll(value);

          map.put(key, c);
        } else {
          List<Object> newList = new ArrayList<>();
          newList.add(existingValue);
          newList.addAll(value);

          map.put(key, newList);
        }
      } else {
        map.put(key, value);
      }
    }

    @Override
    public Collection<ExtractionWithProperties<String>> extractText(Document doc) {
      return List.of(new ExtractionWithProperties<>(doc.text()));
    }

    @Override
    public Collection<ExtractionWithProperties<BufferedImage>> extractImages(Document doc) {
      List<ExtractionWithProperties<BufferedImage>> images = new ArrayList<>();

      int imageNumber = 0;
      for (Element i : doc.getElementsByTag("img")) {
        imageNumber++;

        String src = i.attr("src");
        if (src.isBlank()) continue;

        Map<String, Object> properties = new HashMap<>();

        byte[] data;

        if (src.startsWith("data:image/")) {
          String[] parts = src.split(",", 2);

          data = Base64.getDecoder().decode(parts[1]);
        } else {
          URL url;
          try {
            url = new URL(src);
          } catch (MalformedURLException e) {
            logger.error("Image source '" + src + "' is not a valid URL", e);
            continue;
          }

          try (InputStream is = url.openStream()) {
            data = IOUtils.toByteArray(is);
          } catch (IOException e) {
            logger.error("Unable to read image {} from URL", src, e);
            continue;
          }

          properties.put(PropertyKeys.PROPERTY_KEY_NAME, src.substring(src.lastIndexOf('/') + 1));
        }

        BufferedImage bImg;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
          bImg = ImageIO.read(bais);
        } catch (Exception e) {
          logger.error("Unable to read image from {}", src, e);
          continue;
        }

        if (bImg == null) {
          logger.warn("Null image {} extracted from document", src);
          continue;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
          Metadata imageMetadata = ImageMetadataReader.readMetadata(bais);
          properties.putAll(toMap(imageMetadata));
        } catch (ImageProcessingException | IOException e) {
          logger.warn("Unable to extract metadata from image {}", src, e);
        }

        properties.put(PropertyKeys.PROPERTY_KEY_TITLE, i.attr("title"));
        properties.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);

        if ("figure".equals(i.parent().tagName().toLowerCase())) {
          Element caption = i.parent().getElementsByTag("figcaption").first();
          if (caption != null) {
            properties.put(PropertyKeys.PROPERTY_KEY_DESCRIPTION, caption.text());
          }
        }

        i.attributes()
            .forEach(
                a -> {
                  if ("title".equalsIgnoreCase(a.getKey())) return;

                  properties.put("html" + METADATA_SEPARATOR + a.getKey(), a.getValue());
                });

        images.add(new ExtractionWithProperties<>(bImg, properties));
      }

      return images;
    }
  }
}
