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
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Table;
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
import javax.imageio.ImageIO;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hpsf.extractor.HPSFPropertiesExtractor;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.sl.extractor.SlideShowExtractor;

@ComponentName("PowerPoint (PPT) Extractor")
@ComponentDescription("Extracts image and text from PowerPoint (*.ppt) files")
@ComponentTags({"documents", "powerpoint", "ppt", "extractor", "text", "images", "metadata"})
@SettingsClass(DocumentExtractorSettings.class)
public class PptExtractor
    extends AbstractDocumentExtractorDescriptor<PptExtractor.Processor, DocumentExtractorSettings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<HSLFSlideShow, DocumentExtractorSettings> {

    private final Map<String, HSLFSlideShow> cache = new HashMap<>();

    public Processor(Context context, DocumentExtractorSettings settings) {
      super(context, settings);
    }

    @Override
    public void reset() {
      cache.clear();
    }

    @Override
    protected boolean isTablesSupported() {
      return false;
    }

    @Override
    protected boolean acceptFile(FileContent file) {
      HSLFSlideShow doc;
      try {
        doc = new HSLFSlideShow(new FileInputStream(file.getData()));
      } catch (Exception e) {
        log().debug("FileContent {} not accepted due to: {}", file.getId(), e.getMessage());
        return false;
      }

      cache.put(file.getId(), doc);
      return true;
    }

    @Override
    protected boolean acceptInputStream(InputStreamContent inputStream) {
      HSLFSlideShow doc;
      try {
        doc = new HSLFSlideShow(inputStream.getData());
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
    protected HSLFSlideShow extractDocument(FileContent file) throws IOException {
      if (cache.containsKey(file.getId())) {
        return cache.get(file.getId());
      } else {
        return new HSLFSlideShow(new FileInputStream(file.getData()));
      }
    }

    @Override
    protected HSLFSlideShow extractDocument(InputStreamContent inputStreamContent)
        throws IOException {
      if (cache.containsKey(inputStreamContent.getId())) {
        return cache.get(inputStreamContent.getId());
      } else {
        return new HSLFSlideShow(inputStreamContent.getData());
      }
    }

    @Override
    protected Map<String, Object> extractMetadata(HSLFSlideShow doc) {
      Map<String, Object> props = new HashMap<>();

      try (HPSFPropertiesExtractor propsEx = new HPSFPropertiesExtractor(doc)) {
        propsEx.setCloseFilesystem(false);

        SummaryInformation si = propsEx.getSummaryInformation();

        props.put(DocumentProperties.APPLICATION, si.getApplicationName());
        props.put(DocumentProperties.AUTHOR, si.getAuthor());
        props.put(DocumentProperties.CHARACTER_COUNT, si.getCharCount());
        props.put(DocumentProperties.KEYWORDS, si.getKeywords());
        props.put(DocumentProperties.COMMENTS, si.getComments());
        props.put(DocumentProperties.CREATION_DATE, toTemporal(si.getCreateDateTime()));
        props.put(DocumentProperties.EDITING_DURATION, si.getEditTime());
        props.put(DocumentProperties.LAST_MODIFIED_BY, si.getLastAuthor());
        props.put(DocumentProperties.LAST_PRINTED_DATE, toTemporal(si.getLastPrinted()));
        props.put(DocumentProperties.LAST_MODIFIED_DATE, toTemporal(si.getLastSaveDateTime()));
        props.put(DocumentProperties.PAGE_COUNT, si.getPageCount());
        props.put(DocumentProperties.REVISION, si.getRevNumber());
        switch (si.getSecurity()) {
            // 0 = No security, so let's ignore
          case 1:
            props.put(DocumentProperties.SECURITY, "passwordProtected");
            break;
          case 2:
            props.put(DocumentProperties.SECURITY, "readOnlyRecommended");
            break;
          case 4:
            props.put(DocumentProperties.SECURITY, "readOnlyEnforced");
            break;
          case 8:
            props.put(DocumentProperties.SECURITY, "lockedForAnnotations");
            break;
        }
        props.put(DocumentProperties.SUBJECT, si.getSubject());
        props.put(PropertyKeys.PROPERTY_KEY_TITLE, si.getTitle());
        props.put(DocumentProperties.TEMPLATE, si.getTemplate());
        props.put(DocumentProperties.WORD_COUNT, si.getWordCount());

        DocumentSummaryInformation di = propsEx.getDocSummaryInformation();
        props.put(DocumentProperties.APPLICATION_VERSION, di.getApplicationVersion());
        props.put(DocumentProperties.CATEGORY, di.getCategory());
        props.put(DocumentProperties.COMPANY, di.getCompany());
        props.put(DocumentProperties.CONTENT_STATUS, di.getContentStatus());
        props.put(DocumentProperties.CONTENT_TYPE, di.getContentType());
        props.put(DocumentProperties.BYTE_COUNT, di.getByteCount());
        props.put(DocumentProperties.CHARACTER_COUNT_WS, di.getCharCountWithSpaces());
        props.put(DocumentProperties.DOCUMENT_VERSION, di.getDocumentVersion());
        props.put(DocumentProperties.HIDDEN_COUNT, di.getHiddenCount());
        props.put(PropertyKeys.PROPERTY_KEY_LANGUAGE, di.getLanguage());
        props.put(DocumentProperties.LINE_COUNT, di.getLineCount());
        props.put(DocumentProperties.MANAGER, di.getManager());
        props.put(DocumentProperties.MULTIMEDIA_CLIP_COUNT, di.getMMClipCount());
        props.put(DocumentProperties.NOTE_COUNT, di.getNoteCount());
        props.put(DocumentProperties.PARAGRAPH_COUNT, di.getParCount());
        props.put(DocumentProperties.PRESENTATION_FORMAT, di.getPresentationFormat());
        props.put(DocumentProperties.SLIDE_COUNT, di.getSlideCount());

        di.getCustomProperties()
            .forEach((k, v) -> props.put(DocumentProperties.CUSTOM_PREFIX + k, v));

        // Remove any values that are 0, which POI uses to indicate null for integers
        props.values().removeIf(o -> Integer.valueOf(0).equals(o));
      } catch (IOException e) {
        throw new ProcessingException("Error extracting metadata", e);
      }

      return props;
    }

    @Override
    protected Collection<ExtractionWithProperties<String>> extractText(HSLFSlideShow doc) {
      List<ExtractionWithProperties<String>> extractedText = new ArrayList<>();

      try (SlideShowExtractor<HSLFShape, HSLFTextParagraph> notesExtractor =
              new SlideShowExtractor<>(doc);
          SlideShowExtractor<HSLFShape, HSLFTextParagraph> commentsExtractor =
              new SlideShowExtractor<>(doc);
          SlideShowExtractor<HSLFShape, HSLFTextParagraph> slideExtractor =
              new SlideShowExtractor<>(doc)) {

        slideExtractor.setCloseFilesystem(false);
        slideExtractor.setCommentsByDefault(false);
        slideExtractor.setMasterByDefault(true);
        slideExtractor.setNotesByDefault(false);
        slideExtractor.setSlidesByDefault(true);

        notesExtractor.setCloseFilesystem(false);
        notesExtractor.setCommentsByDefault(false);
        notesExtractor.setMasterByDefault(false);
        notesExtractor.setNotesByDefault(true);
        notesExtractor.setSlidesByDefault(false);

        commentsExtractor.setCloseFilesystem(false);
        commentsExtractor.setCommentsByDefault(true);
        commentsExtractor.setMasterByDefault(false);
        commentsExtractor.setNotesByDefault(false);
        commentsExtractor.setSlidesByDefault(false);

        for (HSLFSlide slide : doc.getSlides()) {
          // Extract Slides
          String slideText = slideExtractor.getText(slide);

          if (!slideText.isBlank()) {
            Map<String, Object> slideProperties = new HashMap<>();
            slideProperties.put(PropertyKeys.PROPERTY_KEY_NAME, slide.getSlideName());
            slideProperties.put(PropertyKeys.PROPERTY_KEY_PAGE, slide.getSlideNumber());
            slideProperties.put(PropertyKeys.PROPERTY_KEY_SUBTYPE, "slide");

            extractedText.add(new ExtractionWithProperties<>(slideText, slideProperties));
          }

          // Extract Notes
          String notesText = notesExtractor.getText(slide);

          if (!notesText.isBlank()) {
            Map<String, Object> notesProperties = new HashMap<>();
            notesProperties.put(PropertyKeys.PROPERTY_KEY_NAME, slide.getSlideName());
            notesProperties.put(PropertyKeys.PROPERTY_KEY_PAGE, slide.getSlideNumber());
            notesProperties.put(PropertyKeys.PROPERTY_KEY_SUBTYPE, "note");

            extractedText.add(new ExtractionWithProperties<>(notesText, notesProperties));
          }

          // Extract Comments
          String commentsText = commentsExtractor.getText(slide);

          if (!commentsText.isBlank()) {
            Map<String, Object> commentsProperties = new HashMap<>();
            commentsProperties.put(PropertyKeys.PROPERTY_KEY_NAME, slide.getSlideName());
            commentsProperties.put(PropertyKeys.PROPERTY_KEY_PAGE, slide.getSlideNumber());
            commentsProperties.put(PropertyKeys.PROPERTY_KEY_SUBTYPE, "comment");

            extractedText.add(new ExtractionWithProperties<>(commentsText, commentsProperties));
          }
        }
      } catch (IOException e) {
        throw new ProcessingException("Error extracting text for " + doc.getClass().getName(), e);
      }

      return extractedText;
    }

    @Override
    protected Collection<ExtractionWithProperties<BufferedImage>> extractImages(HSLFSlideShow doc) {
      List<ExtractionWithProperties<BufferedImage>> extractedImages = new ArrayList<>();

      for (HSLFPictureData picture : doc.getPictureData()) {
        BufferedImage bImg;
        try {
          bImg = ImageIO.read(new ByteArrayInputStream(picture.getData()));
        } catch (IOException e) {
          log().warn("Unable to extract image {} from document", picture.getIndex(), e);
          continue;
        }

        if (bImg == null) {
          log().warn("Null image {} extracted from document", picture.getIndex());
          continue;
        }

        Map<String, Object> properties = new HashMap<>();

        try {
          Metadata imageMetadata =
              ImageMetadataReader.readMetadata(new ByteArrayInputStream(picture.getData()));
          properties.putAll(toMap(imageMetadata));
        } catch (ImageProcessingException | IOException e) {
          log().warn("Unable to extract metadata from image {}", picture.getIndex(), e);
        }

        properties.put(PropertyKeys.PROPERTY_KEY_INDEX, picture.getIndex());
        properties.put(PropertyKeys.PROPERTY_KEY_MIMETYPE, picture.getContentType());

        extractedImages.add(new ExtractionWithProperties<>(bImg, properties));
      }

      return extractedImages;
    }

    @Override
    protected Collection<ExtractionWithProperties<Table>> extractTables(HSLFSlideShow doc)
        throws ProcessingException {
      // TODO: Extract tables from PPT
      return Collections.emptyList();
    }
  }
}
