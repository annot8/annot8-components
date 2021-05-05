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
import io.annot8.common.data.content.DefaultRow;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.components.documents.data.ExtractionWithProperties;
import io.annot8.conventions.PropertyKeys;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawFrame;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawImage;
import org.odftoolkit.odfdom.incubator.meta.OdfMetaDocumentStatistic;
import org.odftoolkit.odfdom.incubator.meta.OdfOfficeMeta;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extracts content from ODT files
 *
 * <p>The text extraction is simplistic, and whilst it should capture the majority of text, no
 * guarantees are given with regards to formatting or ordering of text. In particular, all headers
 * will be placed (once) at the start of the extracted text, and all footers will be placed (once)
 * at the end of the extracted text.
 */
@ComponentName("Open Document Text (ODT) Extractor")
@ComponentDescription("Extracts image and text from Open Document Text (*.odt) files")
@ComponentTags({
  "documents",
  "opendocument",
  "odt",
  "extractor",
  "text",
  "images",
  "metadata",
  "tables"
})
@SettingsClass(DocumentExtractorSettings.class)
public class OdtExtractor
    extends AbstractDocumentExtractorDescriptor<OdtExtractor.Processor, DocumentExtractorSettings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<OdfTextDocument, DocumentExtractorSettings> {

    private final Map<String, OdfTextDocument> cache = new HashMap<>();

    public Processor(Context context, DocumentExtractorSettings settings) {
      super(context, settings);
    }

    @Override
    public void reset() {
      cache.clear();
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
      OdfTextDocument doc;
      try {
        doc = OdfTextDocument.loadDocument(file.getData());
      } catch (Exception e) {
        log().debug("FileContent {} not accepted due to: {}", file.getId(), e.getMessage());
        return false;
      }

      cache.put(file.getId(), doc);
      return true;
    }

    @Override
    public boolean acceptInputStream(InputStreamContent inputStream) {
      OdfTextDocument doc;
      try {
        doc = OdfTextDocument.loadDocument(inputStream.getData());
      } catch (Exception e) {
        log()
            .debug(
                "InputStreamContent {} not accepted due to: {}",
                inputStream.getId(),
                e.getMessage());
        return false;
      }

      cache.put(inputStream.getId(), doc);
      return true;
    }

    @Override
    public OdfTextDocument extractDocument(FileContent file) throws IOException {
      if (cache.containsKey(file.getId())) {
        return cache.get(file.getId());
      } else {
        try {
          return OdfTextDocument.loadDocument(file.getData());
        } catch (Exception e) {
          throw new IOException("Unable to read ODT document", e);
        }
      }
    }

    @Override
    public OdfTextDocument extractDocument(InputStreamContent inputStreamContent)
        throws IOException {
      if (cache.containsKey(inputStreamContent.getId())) {
        return cache.get(inputStreamContent.getId());
      } else {
        try {
          return OdfTextDocument.loadDocument(inputStreamContent.getData());
        } catch (Exception e) {
          throw new IOException("Unable to read ODT document", e);
        }
      }
    }

    @Override
    public Map<String, Object> extractMetadata(OdfTextDocument doc) {
      Map<String, Object> metadata = new HashMap<>();

      OdfOfficeMeta oom = doc.getOfficeMetadata();

      metadata.put(DocumentProperties.CREATION_DATE, toTemporal(oom.getCreationDate()));
      metadata.put(DocumentProperties.CREATOR, oom.getCreator());
      metadata.put(DocumentProperties.LAST_MODIFIED_DATE, toTemporal(oom.getDate()));
      metadata.put(PropertyKeys.PROPERTY_KEY_DESCRIPTION, oom.getDescription());
      metadata.put(DocumentProperties.REVISION, oom.getEditingCycles());
      metadata.put(DocumentProperties.EDITING_DURATION, oom.getEditingDuration().getValue());
      metadata.put(DocumentProperties.GENERATOR, oom.getGenerator());
      metadata.put(DocumentProperties.INITIAL_CREATOR, oom.getInitialCreator());
      metadata.put(DocumentProperties.KEYWORDS, oom.getKeywords());
      metadata.put(PropertyKeys.PROPERTY_KEY_LANGUAGE, oom.getLanguage());
      metadata.put(DocumentProperties.LAST_PRINTED_DATE, oom.getPrintDate());
      metadata.put(DocumentProperties.LAST_PRINTED_BY, oom.getPrintedBy());
      metadata.put(DocumentProperties.SUBJECT, oom.getSubject());
      metadata.put(PropertyKeys.PROPERTY_KEY_TITLE, oom.getTitle());

      if (oom.getTemplate() != null && oom.getTemplate().getMetaTemplateElement() != null)
        metadata.put(
            DocumentProperties.TEMPLATE,
            oom.getTemplate().getMetaTemplateElement().getXlinkHrefAttribute());

      // Custom properties
      if (oom.getUserDefinedDataNames() != null) {
        for (String customKey : oom.getUserDefinedDataNames()) {
          metadata.put(
              DocumentProperties.CUSTOM_PREFIX + customKey, oom.getUserDefinedDataValue(customKey));
        }
      }

      // Document statistics
      OdfMetaDocumentStatistic stats = oom.getDocumentStatistic();
      metadata.put(DocumentProperties.CELL_COUNT, stats.getCellCount());
      metadata.put(DocumentProperties.CHARACTER_COUNT, stats.getCharacterCount());
      metadata.put(DocumentProperties.DRAW_COUNT, stats.getDrawCount());
      metadata.put(DocumentProperties.FRAME_COUNT, stats.getFrameCount());
      metadata.put(DocumentProperties.IMAGE_COUNT, stats.getImageCount());
      metadata.put(DocumentProperties.NW_CHARACTER_COUNT, stats.getNonWhitespaceCharacterCount());
      metadata.put(DocumentProperties.OBJECT_COUNT, stats.getObjectCount());
      metadata.put(DocumentProperties.OLE_OBJECT_COUNT, stats.getOleObjectCount());
      metadata.put(DocumentProperties.PAGE_COUNT, stats.getPageCount());
      metadata.put(DocumentProperties.PARAGRAPH_COUNT, stats.getParagraphCount());
      metadata.put(DocumentProperties.ROW_COUNT, stats.getRowCount());
      metadata.put(DocumentProperties.SENTENCE_COUNT, stats.getSentenceCount());
      metadata.put(DocumentProperties.SYLLABLE_COUNT, stats.getSyllableCount());
      metadata.put(DocumentProperties.TABLE_COUNT, stats.getTableCount());
      metadata.put(DocumentProperties.WORD_COUNT, stats.getWordCount());

      return metadata;
    }

    @Override
    public Collection<ExtractionWithProperties<String>> extractText(OdfTextDocument doc) {

      StringBuilder sb = new StringBuilder();

      // Get Headers - place them at the top
      NodeList children;
      try {
        children = doc.getStylesDom().getElementsByTagName("style:header");
      } catch (Exception e) {
        throw new ProcessingException("Unable to get Styles DOM from ODT", e);
      }
      recurseNodes(children, sb);

      // Get Content
      try {
        children = doc.getContentRoot().getChildNodes();
      } catch (Exception e) {
        throw new ProcessingException("Unable to get Content Root from ODT", e);
      }
      recurseNodes(children, sb);

      // Get Footers - place them at the bottom
      try {
        children = doc.getStylesDom().getElementsByTagName("style:footer");
      } catch (Exception e) {
        throw new ProcessingException("Unable to get Styles DOM from ODT", e);
      }

      recurseNodes(children, sb);

      return List.of(new ExtractionWithProperties<>(sb.toString()));
    }

    private void recurseNodes(NodeList nodes, StringBuilder builder) {
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);

        // Handle spaces
        if ("text:s".equals(node.getNodeName())) builder.append(" ");

        // Handle lists
        if ("text:list-item".equals(node.getNodeName())) {
          int levelCount = 0;
          Node currNode = node;
          while ((currNode = currNode.getParentNode()) != null) {
            if ("text:list".equals(currNode.getNodeName())) levelCount++;
          }

          builder.append("\t".repeat(levelCount));
          builder.append("* ");
        }

        // Handle text
        if (node.getNodeType() == Node.TEXT_NODE) {
          builder.append(node.getTextContent());
        } else if (node.hasChildNodes()) {
          recurseNodes(node.getChildNodes(), builder);

          String nodeName = node.getNodeName();
          if ("text:p".equals(nodeName) || "text:h".equals(nodeName)) builder.append("\n");
        }
      }
    }

    @Override
    public Collection<ExtractionWithProperties<BufferedImage>> extractImages(OdfTextDocument doc) {
      List<ExtractionWithProperties<BufferedImage>> extractedImages = new ArrayList<>();

      int imageNumber = 0;
      NodeList frames;
      try {
        frames = doc.getContentRoot().getElementsByTagName("draw:frame");
      } catch (Exception e) {
        throw new ProcessingException("Unable to get Content Root from ODT", e);
      }

      for (int i = 0; i < frames.getLength(); i++) {
        OdfDrawFrame frame = (OdfDrawFrame) frames.item(i);
        String frameName = frame.getAttribute("draw:name");

        NodeList images = frame.getElementsByTagName("draw:image");
        for (int j = 0; j < images.getLength(); j++) {
          imageNumber++;

          OdfDrawImage image = (OdfDrawImage) images.item(i);
          String href = image.getAttribute("xlink:href");

          BufferedImage bImg;
          try {
            bImg = ImageIO.read(doc.getPackage().getInputStream(href));
          } catch (IOException e) {
            log().warn("Unable to extract image {} from document", imageNumber, e);
            continue;
          }

          if (bImg == null) {
            log().warn("Null image {} extracted from document", imageNumber);
            continue;
          }

          Map<String, Object> properties = new HashMap<>();

          try {
            Metadata imageMetadata =
                ImageMetadataReader.readMetadata(doc.getPackage().getInputStream(href));
            properties.putAll(toMap(imageMetadata));
          } catch (ImageProcessingException | IOException e) {
            log().warn("Unable to extract metadata from image {}", imageNumber, e);
          }

          properties.put(PropertyKeys.PROPERTY_KEY_NAME, frameName);
          properties.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);
          properties.put(
              PropertyKeys.PROPERTY_KEY_TITLE,
              getValueOfFirstElement(frame.getElementsByTagName("svg:title")));
          properties.put(
              PropertyKeys.PROPERTY_KEY_DESCRIPTION,
              getValueOfFirstElement(frame.getElementsByTagName("svg:desc")));

          extractedImages.add(new ExtractionWithProperties<>(bImg, properties));
        }
      }

      return extractedImages;
    }

    @Override
    public Collection<ExtractionWithProperties<Table>> extractTables(OdfTextDocument doc)
        throws ProcessingException {
      return doc.getTableList().stream()
          .map(Processor::transformTable)
          .collect(Collectors.toList());
    }

    private static ExtractionWithProperties<Table> transformTable(OdfTable table) {
      Map<String, Object> props = new HashMap<>();

      String name = table.getTableName();
      if (name != null && !name.isBlank()) props.put(PropertyKeys.PROPERTY_KEY_NAME, name);

      return new ExtractionWithProperties<>(new OdtTable(table), props);
    }

    private String getValueOfFirstElement(NodeList nodeList) {
      if (nodeList.getLength() == 0) return null;
      return nodeList.item(0).getTextContent().strip();
    }
  }

  public static class OdtTable implements Table {
    private final List<Row> rows;
    private final List<String> columnNames;

    public OdtTable(OdfTable t) {
      int headerRows = Math.max(1, t.getHeaderRowCount());
      List<Row> rows = new ArrayList<>(t.getRowCount() - headerRows);
      List<String> columnNames = Collections.emptyList();

      for (int i = 0; i < t.getRowCount(); i++) {
        if (i == 0) {
          OdfTableRow headerRow = t.getRowByIndex(i);

          List<String> header = new ArrayList<>(headerRow.getCellCount());
          for (int col = 0; col < headerRow.getCellCount(); col++) {
            OdfTableCell headerCell = headerRow.getCellByIndex(col);
            header.add(headerCell.getDisplayText());
          }

          columnNames = header;

          continue;
        }

        if (i < t.getHeaderRowCount()) {
          continue;
        }

        OdfTableRow row = t.getRowByIndex(i);

        List<Object> values = new ArrayList<>(row.getCellCount());
        for (int col = 0; col < row.getCellCount(); col++) {
          OdfTableCell cell = row.getCellByIndex(col);

          switch (cell.getValueType()) {
            case "boolean":
              values.add(cell.getBooleanValue());
              break;
            case "currency":
              String currencyPrefix = "";
              if (cell.getCurrencyCode() != null && !cell.getCurrencyCode().isBlank()) {
                currencyPrefix = cell.getCurrencyCode() + " ";
              } else if (cell.getCurrencySymbol() != null && !cell.getCurrencySymbol().isBlank()) {
                currencyPrefix = cell.getCurrencySymbol();
              }
              values.add(currencyPrefix + cell.getCurrencyValue());
              break;
            case "date":
              values.add(
                  cell.getDateValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
              break;
            case "float":
              values.add(cell.getDoubleValue());
              break;
            case "percentage":
              values.add(cell.getPercentageValue());
              break;
            case "string":
              values.add(cell.getStringValue());
              break;
            case "time":
              values.add(
                  cell.getDateValue().toInstant().atZone(ZoneId.systemDefault()).toLocalTime());
              break;
            default:
              values.add(cell.getDisplayText());
          }
        }

        rows.add(new DefaultRow(i - headerRows, columnNames, values));
      }

      this.rows = Collections.unmodifiableList(rows);
      this.columnNames = columnNames;
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
}
