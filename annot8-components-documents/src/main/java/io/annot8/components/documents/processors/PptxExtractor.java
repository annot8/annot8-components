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
import java.util.*;
import javax.imageio.ImageIO;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;

@ComponentName("PowerPoint (PPTX) Extractor")
@ComponentDescription("Extracts image and text from PowerPoint (*.pptx) files")
@ComponentTags({"documents", "powerpoint", "pptx", "extractor", "text", "images", "metadata"})
@SettingsClass(DocumentExtractorSettings.class)
public class PptxExtractor extends AbstractDocumentExtractorDescriptor<PptxExtractor.Processor> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor extends AbstractDocumentExtractorProcessor<XMLSlideShow> {
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
      return file.getData().getName().toLowerCase().endsWith(".pptx");
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

      // FIXME: This only checks whether it is an OOXML, not that it is a PowerPoint Document
      return FileMagic.OOXML == fm;
    }

    @Override
    public XMLSlideShow extractDocument(FileContent file) throws IOException {
      return XSLFSlideShowFactory.createSlideShow(new FileInputStream(file.getData()));
    }

    @Override
    public XMLSlideShow extractDocument(InputStreamContent inputStreamContent) throws IOException {
      return XSLFSlideShowFactory.createSlideShow(inputStreamContent.getData());
    }

    @Override
    public Map<String, Object> extractMetadata(XMLSlideShow doc) {
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
    public Collection<ExtractionWithProperties<String>> extractText(XMLSlideShow doc) {
      List<ExtractionWithProperties<String>> extractedText = new ArrayList<>();

      SlideShowExtractor<XSLFShape, XSLFTextParagraph> slideExtractor =
          new SlideShowExtractor<>(doc);
      slideExtractor.setCommentsByDefault(false);
      slideExtractor.setMasterByDefault(true);
      slideExtractor.setNotesByDefault(false);
      slideExtractor.setSlidesByDefault(true);

      SlideShowExtractor<XSLFShape, XSLFTextParagraph> notesExtractor =
          new SlideShowExtractor<>(doc);
      notesExtractor.setCommentsByDefault(false);
      notesExtractor.setMasterByDefault(false);
      notesExtractor.setNotesByDefault(true);
      notesExtractor.setSlidesByDefault(false);

      SlideShowExtractor<XSLFShape, XSLFTextParagraph> commentsExtractor =
          new SlideShowExtractor<>(doc);
      commentsExtractor.setCommentsByDefault(true);
      commentsExtractor.setMasterByDefault(false);
      commentsExtractor.setNotesByDefault(false);
      commentsExtractor.setSlidesByDefault(false);

      for (XSLFSlide slide : doc.getSlides()) {
        // Extract Slides
        String slideText = slideExtractor.getText(slide);

        if (!slideText.isBlank()) {
          Map<String, Object> slideProperties = new HashMap<>();
          slideProperties.put(PropertyKeys.PROPERTY_KEY_NAME, slide.getSlideName());
          slideProperties.put(PropertyKeys.PROPERTY_KEY_PAGE, slide.getSlideNumber());
          slideProperties.put(PropertyKeys.PROPERTY_KEY_TITLE, slide.getTitle());
          slideProperties.put(PropertyKeys.PROPERTY_KEY_SUBTYPE, "slide");

          extractedText.add(new ExtractionWithProperties<>(slideText, slideProperties));
        }

        // Extract Notes
        String notesText = notesExtractor.getText(slide);

        if (!notesText.isBlank()) {
          Map<String, Object> notesProperties = new HashMap<>();
          notesProperties.put(PropertyKeys.PROPERTY_KEY_PAGE, slide.getSlideNumber());
          notesProperties.put(PropertyKeys.PROPERTY_KEY_SUBTYPE, "note");

          extractedText.add(new ExtractionWithProperties<>(notesText, notesProperties));
        }

        // Extract Comments
        String commentsText = commentsExtractor.getText(slide);

        if (!commentsText.isBlank()) {
          Map<String, Object> commentsProperties = new HashMap<>();
          commentsProperties.put(PropertyKeys.PROPERTY_KEY_PAGE, slide.getSlideNumber());
          commentsProperties.put(PropertyKeys.PROPERTY_KEY_SUBTYPE, "comment");

          extractedText.add(new ExtractionWithProperties<>(commentsText, commentsProperties));
        }
      }

      return extractedText;
    }

    @Override
    public Collection<ExtractionWithProperties<BufferedImage>> extractImages(XMLSlideShow doc) {
      List<ExtractionWithProperties<BufferedImage>> extractedImages = new ArrayList<>();

      for (XSLFPictureData picture : doc.getPictureData()) {
        BufferedImage bImg;
        try {
          bImg = ImageIO.read(new ByteArrayInputStream(picture.getData()));
        } catch (IOException e) {
          logger.warn("Unable to extract image {} from document", picture.getIndex() + 1, e);
          continue;
        }

        Map<String, Object> properties = new HashMap<>();

        try {
          Metadata imageMetadata =
              ImageMetadataReader.readMetadata(new ByteArrayInputStream(picture.getData()));
          properties.putAll(toMap(imageMetadata));
        } catch (ImageProcessingException | IOException e) {
          logger.warn("Unable to extract metadata from image {}", picture.getIndex() + 1, e);
        }

        properties.put(PropertyKeys.PROPERTY_KEY_INDEX, picture.getIndex() + 1);
        properties.put(PropertyKeys.PROPERTY_KEY_MIMETYPE, picture.getContentType());
        properties.put(PropertyKeys.PROPERTY_KEY_NAME, picture.getFileName());

        extractedImages.add(new ExtractionWithProperties<>(bImg, properties));
      }

      return extractedImages;
    }
  }
}
