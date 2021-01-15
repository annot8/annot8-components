/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import com.drew.metadata.Metadata;
import com.drew.metadata.xmp.XmpReader;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.components.documents.data.ExtractionWithProperties;
import io.annot8.conventions.PropertyKeys;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.slf4j.Logger;

@ComponentName("PDF Extractor")
@ComponentDescription("Extracts image and text from PDF (*.pdf) files")
@ComponentTags({"documents", "pdf", "extractor", "text", "images", "metadata"})
@SettingsClass(DocumentExtractorSettings.class)
public class PdfExtractor extends AbstractDocumentExtractorDescriptor<PdfExtractor.Processor> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor extends AbstractDocumentExtractorProcessor<PDDocument> {
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
      return file.getData().getName().toLowerCase().endsWith(".pdf");
    }

    @Override
    public boolean acceptInputStream(InputStreamContent inputStream) {
      try (InputStream is = inputStream.getData()) {
        return FileMagic.valueOf(new BufferedInputStream(is)) == FileMagic.PDF;
      } catch (IOException ioe) {
        return false;
      }
    }

    @Override
    public PDDocument extractDocument(FileContent file) throws IOException {
      return PDDocument.load(file.getData());
    }

    @Override
    public PDDocument extractDocument(InputStreamContent inputStreamContent) throws IOException {
      return PDDocument.load(inputStreamContent.getData());
    }

    @Override
    public Map<String, Object> extractMetadata(PDDocument doc) {
      Map<String, Object> metadata = new HashMap<>();

      PDDocumentInformation info = doc.getDocumentInformation();

      // Haven't extracted the "trapped" property, as it doesn't seem useful

      metadata.put(DocumentProperties.AUTHOR, info.getAuthor());
      metadata.put(DocumentProperties.CREATION_DATE, toTemporal(info.getCreationDate()));
      metadata.put(DocumentProperties.CREATOR, info.getCreator());
      metadata.put(DocumentProperties.KEYWORDS, info.getKeywords());
      metadata.put(DocumentProperties.LAST_MODIFIED_DATE, toTemporal(info.getModificationDate()));
      metadata.put(DocumentProperties.PRODUCER, info.getProducer());
      metadata.put(DocumentProperties.SUBJECT, info.getSubject());
      metadata.put(PropertyKeys.PROPERTY_KEY_TITLE, info.getTitle());
      for (String key : info.getMetadataKeys()) {
        metadata.put(DocumentProperties.CUSTOM_PREFIX + key, info.getCustomMetadataValue(key));
      }
      metadata.put(DocumentProperties.PAGE_COUNT, doc.getNumberOfPages());

      return metadata;
    }

    @Override
    public Collection<ExtractionWithProperties<String>> extractText(PDDocument doc)
        throws ProcessingException {
      try {
        return List.of(new ExtractionWithProperties<>(new PDFTextStripper().getText(doc)));
      } catch (IOException e) {
        throw new ProcessingException("Unable to extract text from PDF", e);
      }
    }

    @Override
    public Collection<ExtractionWithProperties<BufferedImage>> extractImages(PDDocument doc) {
      ImageExtractor extractor = new ImageExtractor();

      int pageNumber = 0;
      for (PDPage page : doc.getPages()) {
        pageNumber++;

        try {
          extractor.setPageNumber(pageNumber);
          extractor.processPage(page);
        } catch (IOException e) {
          logger.warn("Unable to extract images from page {} of PDF", pageNumber, e);
        }
      }

      return extractor.getExtractedImages();
    }

    private static class ImageExtractor extends PDFStreamEngine {
      private int imageNumber = 0;
      private final List<ExtractionWithProperties<BufferedImage>> extractedImages =
          new ArrayList<>();

      private int pageNumber = -1;

      protected void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
      }

      protected List<ExtractionWithProperties<BufferedImage>> getExtractedImages() {
        return extractedImages;
      }

      @Override
      protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        if ("Do".equals(operator.getName())) {
          COSName objectName = (COSName) operands.get(0);
          PDXObject xobject = getResources().getXObject(objectName);

          if (xobject instanceof PDImageXObject) {
            Map<String, Object> imageMetadata = new HashMap<>();

            PDImageXObject image = (PDImageXObject) xobject;
            PDMetadata metadata = image.getMetadata();

            if (metadata != null) {
              Metadata md = new Metadata();
              XmpReader xmpReader = new XmpReader();

              xmpReader.extract(metadata.toByteArray(), md);
              imageMetadata.putAll(toMap(md));
            }

            imageNumber++;
            imageMetadata.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);
            imageMetadata.put(PropertyKeys.PROPERTY_KEY_PAGE, pageNumber);

            extractedImages.add(new ExtractionWithProperties<>(image.getImage(), imageMetadata));
          } else if (xobject instanceof PDFormXObject) {
            PDFormXObject form = (PDFormXObject) xobject;
            showForm(form);
          }
        } else {
          super.processOperator(operator, operands);
        }
      }
    }
  }
}
