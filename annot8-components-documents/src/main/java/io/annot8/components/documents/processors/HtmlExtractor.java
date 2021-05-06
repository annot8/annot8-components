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
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.api.settings.Description;
import io.annot8.common.data.content.DefaultRow;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.utils.java.ConversionUtils;
import io.annot8.components.documents.data.ExtractionWithProperties;
import io.annot8.conventions.PropertyKeys;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/** Extracts content from HTML files */
@ComponentName("HTML Extractor")
@ComponentDescription("Extracts image and text from HTML (*.html) files")
@ComponentTags({"documents", "html", "extractor", "text", "images", "metadata", "tables"})
@SettingsClass(HtmlExtractor.Settings.class)
public class HtmlExtractor
    extends AbstractDocumentExtractorDescriptor<HtmlExtractor.Processor, HtmlExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, HtmlExtractor.Settings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<Document, HtmlExtractor.Settings> {

    private final HttpClient client;

    public Processor(Context context, HtmlExtractor.Settings settings) {
      super(context, settings);

      client =
          HttpClient.newBuilder()
              .followRedirects(
                  settings.isFollowImageRedirects()
                      ? HttpClient.Redirect.NORMAL
                      : HttpClient.Redirect.NEVER)
              .build();
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
    public boolean isTablesSupported() {
      return true;
    }

    @Override
    public boolean acceptFile(FileContent file) {
      try {
        return FileMagic.valueOf(file.getData()) == FileMagic.HTML;
      } catch (IOException e) {
        return false;
      }
    }

    @Override
    public boolean acceptInputStream(InputStreamContent inputStream) {
      try (InputStream is = new BufferedInputStream(inputStream.getData())) {
        return FileMagic.valueOf(is) == FileMagic.HTML;
      } catch (IOException e) {
        return false;
      }
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
      List<ExtractionWithProperties<String>> extractedText = new ArrayList<>();

      if (settings.getCssQueryText() != null && !settings.getCssQueryText().isBlank()) {
        int i = 0;
        for (Element e : doc.select(settings.getCssQueryText())) {
          if (!e.hasText()) continue;

          Map<String, Object> props = new HashMap<>();
          props.put(PropertyKeys.PROPERTY_KEY_INDEX, i);
          if (e.id() != null && !e.id().isBlank())
            props.put(PropertyKeys.PROPERTY_KEY_IDENTIFIER, e.id());

          extractedText.add(new ExtractionWithProperties<>(e.text(), props));

          i++;
        }
      } else {
        extractedText.add(new ExtractionWithProperties<>(doc.text()));
      }
      return extractedText;
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
          HttpRequest request = HttpRequest.newBuilder().uri(URI.create(src)).build();
          try {
            HttpResponse<byte[]> response =
                client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
              log().warn("Status code {} returned from URL {}", response.statusCode(), src);
              continue;
            }

            data = response.body();
          } catch (Exception e) {
            log().error("Unable to read image from URL {}", src, e);
            continue;
          }

          properties.put(PropertyKeys.PROPERTY_KEY_NAME, src.substring(src.lastIndexOf('/') + 1));
        }

        BufferedImage bImg;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
          bImg = ImageIO.read(bais);
        } catch (Exception e) {
          log().error("Unable to read image from {}", src, e);
          continue;
        }

        if (bImg == null) {
          log().warn("Null image {} extracted from document", src);
          continue;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
          Metadata imageMetadata = ImageMetadataReader.readMetadata(bais);
          properties.putAll(toMap(imageMetadata));
        } catch (ImageProcessingException | IOException e) {
          log().warn("Unable to extract metadata from image {}", src, e);
        }

        properties.put(PropertyKeys.PROPERTY_KEY_TITLE, i.attr("title"));
        properties.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);

        if ("figure".equalsIgnoreCase(i.parent().tagName())) {
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

    @Override
    public Collection<ExtractionWithProperties<Table>> extractTables(Document doc)
        throws ProcessingException {
      return doc.getElementsByTag("table").stream()
          .map(Processor::transformTable)
          .collect(Collectors.toList());
    }

    private static ExtractionWithProperties<Table> transformTable(Element table) {
      Map<String, Object> props = new HashMap<>();

      String desc = table.select("caption").text();
      if (desc != null && !desc.isBlank()) props.put(PropertyKeys.PROPERTY_KEY_DESCRIPTION, desc);

      String lang = table.attr("lang");
      if (lang != null && !lang.isBlank()) props.put(PropertyKeys.PROPERTY_KEY_LANGUAGE, lang);

      String title = table.attr("title");
      if (title != null && !title.isBlank()) props.put(PropertyKeys.PROPERTY_KEY_TITLE, title);

      String id = table.attr("id");
      if (id != null && !id.isBlank()) props.put(PropertyKeys.PROPERTY_KEY_IDENTIFIER, id);

      return new ExtractionWithProperties<>(new HtmlTable(table), props);
    }
  }

  public static class HtmlTable implements Table {
    private final List<Row> rows;
    private final List<String> columnNames;

    public HtmlTable(Element table) {
      List<String> columnNames = Collections.emptyList();

      Element headerRow = table.selectFirst("thead > tr");
      if (headerRow != null) {
        columnNames =
            headerRow.getElementsByTag("th").stream()
                .map(Element::text)
                .collect(Collectors.toList());
      }

      List<Row> rows = new ArrayList<>();
      Elements bodyRows = table.select("tbody > tr");
      for (int i = 0; i < bodyRows.size(); i++) {
        // TODO: Handle column spans?
        List<Object> data =
            bodyRows.get(i).select("td").stream()
                .map(Element::text)
                .map(ConversionUtils::parseString)
                .collect(Collectors.toList());
        rows.add(new DefaultRow(i, columnNames, data));
      }

      this.columnNames = columnNames;
      this.rows = rows;
    }

    @Override
    public int getColumnCount() {
      return columnNames.size();
    }

    @Override
    public int getRowCount() {
      return rows.size();
    }

    @Override
    public Optional<List<String>> getColumnNames() {
      return Optional.of(columnNames);
    }

    @Override
    public Stream<Row> getRows() {
      return rows.stream();
    }
  }

  public static class Settings extends DocumentExtractorSettings {
    private String cssQueryText = "";
    private boolean followImageRedirects = false;

    public Settings() {
      // Default constructor
    }

    public Settings(DocumentExtractorSettings settings) {
      super(settings);
    }

    @Description(
        "If set, then the give CSS Query will be used to select text within the document (otherwise text from the whole document is returned)")
    public String getCssQueryText() {
      return cssQueryText;
    }

    public void setCssQueryText(String cssQueryText) {
      this.cssQueryText = cssQueryText;
    }

    @Description("If true, then redirects will be followed when attempting to download images")
    public boolean isFollowImageRedirects() {
      return followImageRedirects;
    }

    public void setFollowImageRedirects(boolean followImageRedirects) {
      this.followImageRedirects = followImageRedirects;
    }
  }
}
