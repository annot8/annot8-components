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
import java.io.BufferedInputStream;
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
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.slf4j.Logger;

@ComponentName("Word Document (DOC) Extractor")
@ComponentDescription("Extracts image and text from Word Document (*.doc) files")
@ComponentTags({"documents", "word", "doc", "extractor", "text", "images"})
@SettingsClass(DocumentExtractorSettings.class)
public class DocExtractor
    extends AbstractDocumentExtractorDescriptor<DocExtractor.Processor, DocumentExtractorSettings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<HWPFDocument, DocumentExtractorSettings> {
    private final Logger logger = getLogger();

    public Processor(Context context, DocumentExtractorSettings settings) {
      super(context, settings);
    }

    @Override
    public boolean isMetadataSupported() {
      return false;
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
      return file.getData().getName().toLowerCase().endsWith(".doc");
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

      // FIXME: This only checks whether it is an OLE2, not that it is a Word Document
      return FileMagic.OLE2 == fm;
    }

    @Override
    public HWPFDocument extractDocument(FileContent file) throws IOException {
      return new HWPFDocument(new FileInputStream(file.getData()));
    }

    @Override
    public HWPFDocument extractDocument(InputStreamContent inputStreamContent) throws IOException {
      return new HWPFDocument(inputStreamContent.getData());
    }

    @Override
    public Map<String, Object> extractMetadata(HWPFDocument doc) {
      // TODO: Work out best way to extract metadata, as it's not straightforwards

      return Collections.emptyMap();
    }

    @Override
    public Collection<ExtractionWithProperties<String>> extractText(HWPFDocument doc) {
      WordExtractor wordExtractor = new WordExtractor(doc);
      return List.of(new ExtractionWithProperties<>(wordExtractor.getText()));
    }

    @Override
    public Collection<ExtractionWithProperties<BufferedImage>> extractImages(HWPFDocument doc) {
      List<ExtractionWithProperties<BufferedImage>> extractedImages = new ArrayList<>();

      int imageNumber = 0;
      for (Picture p : doc.getPicturesTable().getAllPictures()) {
        imageNumber++;

        BufferedImage bImg;
        try {
          bImg = ImageIO.read(new ByteArrayInputStream(p.getContent()));
        } catch (IOException e) {
          logger.warn("Unable to extract image {} from document", imageNumber, e);
          continue;
        }

        if (bImg == null) {
          logger.warn("Null image {} extracted from document", imageNumber);
          continue;
        }

        Map<String, Object> props = new HashMap<>();

        try {
          Metadata imageMetadata =
              ImageMetadataReader.readMetadata(new ByteArrayInputStream(p.getContent()));
          props.putAll(toMap(imageMetadata));
        } catch (ImageProcessingException | IOException e) {
          logger.warn("Unable to extract metadata from image {}", imageNumber, e);
        }

        props.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);
        props.put(PropertyKeys.PROPERTY_KEY_NAME, p.suggestFullFileName());
        props.put(PropertyKeys.PROPERTY_KEY_DESCRIPTION, p.getDescription());
        props.put(PropertyKeys.PROPERTY_KEY_MIMETYPE, p.getMimeType());

        extractedImages.add(new ExtractionWithProperties<>(bImg, props));
      }

      return extractedImages;
    }

    @Override
    public Collection<ExtractionWithProperties<Table>> extractTables(HWPFDocument doc)
        throws ProcessingException {

      List<ExtractionWithProperties<Table>> ret = new ArrayList<>();

      TableIterator tableIterator = new TableIterator(doc.getRange());
      while (tableIterator.hasNext()) {
        org.apache.poi.hwpf.usermodel.Table tbl = tableIterator.next();
        ret.add(new ExtractionWithProperties<>(new DocTable(tbl)));
      }

      return ret;
    }
  }

  public static class DocTable implements Table {
    private final List<Row> rows;
    private final List<String> columnNames;

    public DocTable(org.apache.poi.hwpf.usermodel.Table t) {
      List<Row> rows = new ArrayList<>(t.numRows());

      List<String> columnNames = Collections.emptyList();
      for (int i = 0; i < t.numRows(); i++) {
        TableRow r = t.getRow(i);

        List<Object> data = new ArrayList<>();
        for (int j = 0; j < r.numCells(); j++) {
          data.add(removeControlCharacters(r.getCell(j).text()));
        }

        if (columnNames.isEmpty() && r.isTableHeader()) {
          columnNames = data.stream().map(Object::toString).collect(Collectors.toList());
        } else {
          Row row = new DefaultRow(i - 1, columnNames, data);
          rows.add(row);
        }
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

    private static String removeControlCharacters(String s) {
      return s.replaceAll("[\u0000-\u001f]", "");
    }
  }
}
