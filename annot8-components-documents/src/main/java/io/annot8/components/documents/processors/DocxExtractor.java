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
import io.annot8.common.utils.java.ConversionUtils;
import io.annot8.components.documents.data.ExtractionWithProperties;
import io.annot8.conventions.PropertyKeys;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

@ComponentName("Word Document (DOCX) Extractor")
@ComponentDescription("Extracts image and text from Word Document (*.docx) files")
@ComponentTags({"documents", "word", "docx", "extractor", "text", "images", "metadata", "tables"})
@SettingsClass(DocumentExtractorSettings.class)
public class DocxExtractor
    extends AbstractDocumentExtractorDescriptor<
        DocxExtractor.Processor, DocumentExtractorSettings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<XWPFDocument, DocumentExtractorSettings> {

    private final Map<String, XWPFDocument> cache = new HashMap<>();

    public Processor(Context context, DocumentExtractorSettings settings) {
      super(context, settings);
    }

    @Override
    public void reset() {
      cache.clear();
    }

    @Override
    protected boolean isMetadataSupported() {
      return true;
    }

    @Override
    protected boolean isTextSupported() {
      return true;
    }

    @Override
    protected boolean isImagesSupported() {
      return true;
    }

    @Override
    protected boolean isTablesSupported() {
      return true;
    }

    @Override
    protected boolean acceptFile(FileContent file) {
      XWPFDocument doc;
      try {
        doc = new XWPFDocument(new FileInputStream(file.getData()));
      } catch (Exception e) {
        log().debug("FileContent {} not accepted due to: {}", file.getId(), e.getMessage());
        return false;
      }

      cache.put(file.getId(), doc);
      return true;
    }

    @Override
    protected boolean acceptInputStream(InputStreamContent inputStream) {
      XWPFDocument doc;
      try {
        doc = new XWPFDocument(inputStream.getData());
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
    protected XWPFDocument extractDocument(FileContent file) throws IOException {
      if (cache.containsKey(file.getId())) {
        return cache.get(file.getId());
      } else {
        return new XWPFDocument(new FileInputStream(file.getData()));
      }
    }

    @Override
    protected XWPFDocument extractDocument(InputStreamContent inputStreamContent)
        throws IOException {
      if (cache.containsKey(inputStreamContent.getId())) {
        return cache.get(inputStreamContent.getId());
      } else {
        return new XWPFDocument(inputStreamContent.getData());
      }
    }

    @Override
    protected Map<String, Object> extractMetadata(XWPFDocument doc) {
      Map<String, Object> metadata = new HashMap<>();

      POIXMLProperties.CoreProperties props = doc.getProperties().getCoreProperties();
      metadata.put(DocumentProperties.CATEGORY, props.getCategory());
      metadata.put(DocumentProperties.CONTENT_STATUS, props.getContentStatus());
      metadata.put(DocumentProperties.CONTENT_TYPE, props.getContentType());
      metadata.put(DocumentProperties.CREATION_DATE, toTemporal(props.getCreated()));
      metadata.put(DocumentProperties.CREATOR, props.getCreator());
      metadata.put(PropertyKeys.PROPERTY_KEY_DESCRIPTION, props.getDescription());
      metadata.put(PropertyKeys.PROPERTY_KEY_IDENTIFIER, props.getIdentifier());
      metadata.put(DocumentProperties.KEYWORDS, props.getKeywords());
      metadata.put(DocumentProperties.LAST_MODIFIED_BY, props.getLastModifiedByUser());
      metadata.put(DocumentProperties.LAST_PRINTED_DATE, toTemporal(props.getLastPrinted()));
      metadata.put(DocumentProperties.LAST_MODIFIED_DATE, toTemporal(props.getModified()));
      metadata.put(DocumentProperties.REVISION, props.getRevision());
      metadata.put(DocumentProperties.SUBJECT, props.getSubject());
      metadata.put(PropertyKeys.PROPERTY_KEY_TITLE, props.getTitle());

      POIXMLProperties.ExtendedProperties extendedProps =
          doc.getProperties().getExtendedProperties();
      metadata.put(DocumentProperties.APPLICATION, extendedProps.getApplication());
      metadata.put(DocumentProperties.APPLICATION_VERSION, extendedProps.getAppVersion());
      metadata.put(DocumentProperties.NW_CHARACTER_COUNT, extendedProps.getCharacters());
      metadata.put(DocumentProperties.CHARACTER_COUNT, extendedProps.getCharactersWithSpaces());
      metadata.put(DocumentProperties.COMPANY, extendedProps.getCompany());
      metadata.put(DocumentProperties.HIDDEN_SLIDE_COUNT, extendedProps.getHiddenSlides());
      metadata.put(DocumentProperties.HYPERLINK_BASE, extendedProps.getHyperlinkBase());
      metadata.put(DocumentProperties.LINE_COUNT, extendedProps.getLines());
      metadata.put(DocumentProperties.MANAGER, extendedProps.getManager());
      metadata.put(DocumentProperties.MULTIMEDIA_CLIP_COUNT, extendedProps.getMMClips());
      metadata.put(DocumentProperties.NOTE_COUNT, extendedProps.getNotes());
      metadata.put(DocumentProperties.PAGE_COUNT, extendedProps.getPages());
      metadata.put(DocumentProperties.PARAGRAPH_COUNT, extendedProps.getParagraphs());
      metadata.put(DocumentProperties.PRESENTATION_FORMAT, extendedProps.getPresentationFormat());
      metadata.put(DocumentProperties.SLIDE_COUNT, extendedProps.getSlides());
      metadata.put(DocumentProperties.TEMPLATE, extendedProps.getTemplate());
      metadata.put(DocumentProperties.EDITING_DURATION, extendedProps.getTotalTime());
      metadata.put(DocumentProperties.WORD_COUNT, extendedProps.getWords());

      // Remove any values that are -1, which POI uses to indicate null for integers
      metadata.values().removeIf(o -> Integer.valueOf(-1).equals(o));

      return metadata;
    }

    @Override
    protected Collection<ExtractionWithProperties<String>> extractText(XWPFDocument doc) {
      // TODO: Should we remove Tables from this?
      try (XWPFWordExtractor wordExtractor = new XWPFWordExtractor(doc)) {
        wordExtractor.setCloseFilesystem(false);
        return List.of(new ExtractionWithProperties<>(wordExtractor.getText()));
      } catch (IOException e) {
        log().warn("Failed to extract text from XWPFDocument", e);
        return List.of();
      }
    }

    @Override
    protected Collection<ExtractionWithProperties<BufferedImage>> extractImages(XWPFDocument doc) {
      List<ExtractionWithProperties<BufferedImage>> extractedImages = new ArrayList<>();

      int imageNumber = 0;
      for (XWPFPictureData p : doc.getAllPictures()) {
        imageNumber++;

        BufferedImage bImg;
        try {
          bImg = ImageIO.read(new ByteArrayInputStream(p.getData()));
        } catch (IOException e) {
          log().warn("Unable to extract image {} from document", imageNumber, e);
          continue;
        }

        if (bImg == null) {
          log().warn("Null image {} extracted from document", imageNumber);
          continue;
        }

        Map<String, Object> props = new HashMap<>();

        try {
          Metadata imageMetadata =
              ImageMetadataReader.readMetadata(new ByteArrayInputStream(p.getData()));
          props.putAll(toMap(imageMetadata));
        } catch (ImageProcessingException | IOException e) {
          log().warn("Unable to extract metadata from image {}", imageNumber, e);
        }

        props.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);
        props.put(PropertyKeys.PROPERTY_KEY_NAME, p.getFileName());

        extractedImages.add(new ExtractionWithProperties<>(bImg, props));
      }

      return extractedImages;
    }

    @Override
    protected Collection<ExtractionWithProperties<Table>> extractTables(XWPFDocument doc)
        throws ProcessingException {
      return doc.getTables().stream().map(Processor::transformTable).collect(Collectors.toList());
    }

    private static ExtractionWithProperties<Table> transformTable(XWPFTable table) {
      return new ExtractionWithProperties<>(new DocxTable(table));
    }
  }

  public static class DocxTable implements Table {
    private final List<Row> rows;
    private final List<String> columnNames;

    protected DocxTable(XWPFTable t) {
      List<Row> tmpRows = new ArrayList<>(t.getNumberOfRows() - 1);
      List<String> tmpColumnNames = Collections.emptyList();

      for (int i = 0; i < t.getNumberOfRows(); i++) {
        XWPFTableRow r = t.getRow(i);

        List<Object> data =
            r.getTableCells().stream()
                .map(XWPFTableCell::getText)
                .map(ConversionUtils::parseString)
                .collect(Collectors.toList());

        if (i == 0) {
          // Assume header row if first row
          tmpColumnNames = data.stream().map(Object::toString).collect(Collectors.toList());
        } else {
          Row row = new DefaultRow(i - 1, tmpColumnNames, data);
          tmpRows.add(row);
        }
      }

      this.rows = Collections.unmodifiableList(tmpRows);
      this.columnNames = Collections.unmodifiableList(tmpColumnNames);
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
