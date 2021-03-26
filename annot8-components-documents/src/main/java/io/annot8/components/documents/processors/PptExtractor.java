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
import javax.imageio.ImageIO;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.slf4j.Logger;

@ComponentName("PowerPoint (PPT) Extractor")
@ComponentDescription("Extracts image and text from PowerPoint (*.ppt) files")
@ComponentTags({"documents", "powerpoint", "ppt", "extractor", "text", "images"})
@SettingsClass(DocumentExtractorSettings.class)
public class PptExtractor
    extends AbstractDocumentExtractorDescriptor<PptExtractor.Processor, DocumentExtractorSettings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<HSLFSlideShow, DocumentExtractorSettings> {
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
      return false;
    }

    @Override
    public boolean acceptFile(FileContent file) {
      return file.getData().getName().toLowerCase().endsWith(".ppt");
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

      // FIXME: This only checks whether it is an OLE2, not that it is a PowerPoint Document
      return FileMagic.OLE2 == fm;
    }

    @Override
    public HSLFSlideShow extractDocument(FileContent file) throws IOException {
      return new HSLFSlideShow(new FileInputStream(file.getData()));
    }

    @Override
    public HSLFSlideShow extractDocument(InputStreamContent inputStreamContent) throws IOException {
      return new HSLFSlideShow(inputStreamContent.getData());
    }

    @Override
    public Map<String, Object> extractMetadata(HSLFSlideShow doc) {
      // TODO: Work out best way to extract metadata, as it's not straightforwards

      return Collections.emptyMap();
    }

    @Override
    public Collection<ExtractionWithProperties<String>> extractText(HSLFSlideShow doc) {
      List<ExtractionWithProperties<String>> extractedText = new ArrayList<>();

      SlideShowExtractor<HSLFShape, HSLFTextParagraph> slideExtractor =
          new SlideShowExtractor<>(doc);
      slideExtractor.setCommentsByDefault(false);
      slideExtractor.setMasterByDefault(true);
      slideExtractor.setNotesByDefault(false);
      slideExtractor.setSlidesByDefault(true);

      SlideShowExtractor<HSLFShape, HSLFTextParagraph> notesExtractor =
          new SlideShowExtractor<>(doc);
      notesExtractor.setCommentsByDefault(false);
      notesExtractor.setMasterByDefault(false);
      notesExtractor.setNotesByDefault(true);
      notesExtractor.setSlidesByDefault(false);

      SlideShowExtractor<HSLFShape, HSLFTextParagraph> commentsExtractor =
          new SlideShowExtractor<>(doc);
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

      return extractedText;
    }

    @Override
    public Collection<ExtractionWithProperties<BufferedImage>> extractImages(HSLFSlideShow doc) {
      List<ExtractionWithProperties<BufferedImage>> extractedImages = new ArrayList<>();

      for (HSLFPictureData picture : doc.getPictureData()) {
        BufferedImage bImg;
        try {
          bImg = ImageIO.read(new ByteArrayInputStream(picture.getData()));
        } catch (IOException e) {
          logger.warn("Unable to extract image {} from document", picture.getIndex(), e);
          continue;
        }

        if (bImg == null) {
          logger.warn("Null image {} extracted from document", picture.getIndex());
          continue;
        }

        Map<String, Object> properties = new HashMap<>();

        try {
          Metadata imageMetadata =
              ImageMetadataReader.readMetadata(new ByteArrayInputStream(picture.getData()));
          properties.putAll(toMap(imageMetadata));
        } catch (ImageProcessingException | IOException e) {
          logger.warn("Unable to extract metadata from image {}", picture.getIndex(), e);
        }

        properties.put(PropertyKeys.PROPERTY_KEY_INDEX, picture.getIndex());
        properties.put(PropertyKeys.PROPERTY_KEY_MIMETYPE, picture.getContentType());

        extractedImages.add(new ExtractionWithProperties<>(bImg, properties));
      }

      return extractedImages;
    }

    @Override
    public Collection<ExtractionWithProperties<Table>> extractTables(HSLFSlideShow doc)
        throws ProcessingException {
      // TODO: Extract tables from PPT
      return Collections.emptyList();
    }
  }
}
