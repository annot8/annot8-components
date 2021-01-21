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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.slf4j.Logger;

@ComponentName("Word Document (DOCX) Extractor")
@ComponentDescription("Extracts image and text from Word Document (*.docx) files")
@ComponentTags({"documents", "word", "docx", "extractor", "text", "images", "metadata"})
@SettingsClass(DocumentExtractorSettings.class)
public class DocxExtractor extends AbstractDocumentExtractorDescriptor<DocxExtractor.Processor> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor extends AbstractDocumentExtractorProcessor<XWPFDocument> {
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
      return file.getData().getName().toLowerCase().endsWith(".docx");
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

      // FIXME: This only checks whether it is an OOXML, not that it is a Word Document
      return FileMagic.OOXML == fm;
    }

    @Override
    public XWPFDocument extractDocument(FileContent file) throws IOException {
      return new XWPFDocument(new FileInputStream(file.getData()));
    }

    @Override
    public XWPFDocument extractDocument(InputStreamContent inputStreamContent) throws IOException {
      return new XWPFDocument(inputStreamContent.getData());
    }

    @Override
    public Map<String, Object> extractMetadata(XWPFDocument doc) {
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
    public Collection<ExtractionWithProperties<String>> extractText(XWPFDocument doc) {
      XWPFWordExtractor wordExtractor = new XWPFWordExtractor(doc);
      return List.of(new ExtractionWithProperties<>(wordExtractor.getText()));
    }

    @Override
    public Collection<ExtractionWithProperties<BufferedImage>> extractImages(XWPFDocument doc) {
      List<ExtractionWithProperties<BufferedImage>> extractedImages = new ArrayList<>();

      int imageNumber = 0;
      for (XWPFPictureData p : doc.getAllPictures()) {
        imageNumber++;

        BufferedImage bImg;
        try {
          bImg = ImageIO.read(new ByteArrayInputStream(p.getData()));
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
              ImageMetadataReader.readMetadata(new ByteArrayInputStream(p.getData()));
          props.putAll(toMap(imageMetadata));
        } catch (ImageProcessingException | IOException e) {
          logger.warn("Unable to extract metadata from image {}", imageNumber, e);
        }

        props.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);
        props.put(PropertyKeys.PROPERTY_KEY_NAME, p.getFileName());

        extractedImages.add(new ExtractionWithProperties<>(bImg, props));
      }

      return extractedImages;
    }
  }
}
