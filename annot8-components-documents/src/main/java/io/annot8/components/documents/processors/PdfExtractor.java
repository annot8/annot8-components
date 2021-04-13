/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import com.drew.metadata.Metadata;
import com.drew.metadata.xmp.XmpReader;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.Annot8RuntimeException;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.api.settings.Description;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Table;
import io.annot8.components.documents.data.ExtractionWithProperties;
import io.annot8.conventions.PropertyKeys;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@SettingsClass(PdfExtractor.Settings.class)
public class PdfExtractor
    extends AbstractDocumentExtractorDescriptor<PdfExtractor.Processor, PdfExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, PdfExtractor.Settings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<PDDocument, PdfExtractor.Settings> {
    private final Logger logger = getLogger();

    private final PDFTextStripper stripper;

    public Processor(Context context, PdfExtractor.Settings settings) {
      super(context, settings);

      if (settings.isExtractText()) {
        try {
          stripper = new PDFTextStripper();
          stripper.setPageStart(settings.getPageStart());
          stripper.setPageEnd(settings.getPageEnd());
          stripper.setParagraphStart(settings.getParagraphStart());
          stripper.setParagraphEnd(settings.getParagraphEnd());
          stripper.setArticleStart(settings.getArticleStart());
          stripper.setArticleEnd(settings.getArticleEnd());
        } catch (IOException ioe) {
          throw new Annot8RuntimeException("Unable to create PDFTextStripper", ioe);
        }
      } else {
        stripper = null;
      }
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
      return false;
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
        return List.of(new ExtractionWithProperties<>(stripper.getText(doc)));
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

    @Override
    public Collection<ExtractionWithProperties<Table>> extractTables(PDDocument doc)
        throws ProcessingException {
      // TODO: Extract tables from PDF
      return Collections.emptyList();
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

      public ImageExtractor() {
        //        addOperator(new Concatenate());
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

            BufferedImage img = image.getImage();
            //            try {
            //              //TODO: This could probably do with being more generic (also handling
            // rotation) - not sure it is correct in all cases
            //              AffineTransform transform = new AffineTransform();
            //              Matrix m = getGraphicsState().getCurrentTransformationMatrix();
            //
            //              //FIXME: Ought to properly apply the AffineTransformation defined in the
            // PDF
            //              //Flip the images where they have negative scale
            //              transform.scale(m.getScaleX() < 0.0 ? -1.0 : 1.0, m.getScaleY() < 0.0 ?
            // -1.0 : 1.0);
            //              transform.translate(m.getScaleX() < 0.0 ? -image.getWidth() : 0.0,
            // m.getScaleY() < 0.0 ? -image.getHeight() : 0.0);
            //
            //              // Apply affine transform
            //              System.out.println(m.createAffineTransform());
            //              AffineTransformOp op = new AffineTransformOp(m.createAffineTransform(),
            // AffineTransformOp.TYPE_BILINEAR);
            //
            //              img = op.createCompatibleDestImage(image.getImage(),
            // image.getImage().getColorModel());
            //              op.filter(image.getImage(), img);
            //            } catch (Exception e) {
            //              //TODO: Log error here
            //              e.printStackTrace();
            //              img = image.getImage();
            //            }

            // Extract metadata
            if (metadata != null) {
              Metadata md = new Metadata();
              XmpReader xmpReader = new XmpReader();

              xmpReader.extract(metadata.toByteArray(), md);
              imageMetadata.putAll(toMap(md));
            }

            imageNumber++;
            imageMetadata.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);
            imageMetadata.put(PropertyKeys.PROPERTY_KEY_PAGE, pageNumber);

            extractedImages.add(new ExtractionWithProperties<>(img, imageMetadata));
          } else if (xobject instanceof PDFormXObject) {
            PDFormXObject form = (PDFormXObject) xobject;
            showForm(form);
          }
        } else {
          //          try {
          super.processOperator(operator, operands);
          //          }catch (Exception e){
          //            // Catch some exceptions thrown by PDFBox, that are caused by the addition
          // of Concatenate() processor in the constructor
          //            // I think we can ignore these, as we're only interested in extracting
          // images
          //          }
        }
      }
    }
  }

  public static class Settings extends DocumentExtractorSettings {
    private String articleStart = "";
    private String articleEnd = "";
    private String pageStart = "";
    private String pageEnd = "";
    private String paragraphStart = "";
    private String paragraphEnd = "\n\n";

    @Override
    public boolean validate() {
      return super.validate()
          && articleStart != null
          && articleEnd != null
          && pageStart != null
          && pageEnd != null
          && paragraphStart != null
          && paragraphEnd != null;
    }

    @Description("String to add at the start of each article")
    public String getArticleStart() {
      return articleStart;
    }

    public void setArticleStart(String articleStart) {
      this.articleStart = articleStart;
    }

    @Description("String to add at the end of each article")
    public String getArticleEnd() {
      return articleEnd;
    }

    public void setArticleEnd(String articleEnd) {
      this.articleEnd = articleEnd;
    }

    @Description("String to add at the start of each article")
    public String getPageStart() {
      return pageStart;
    }

    public void setPageStart(String pageStart) {
      this.pageStart = pageStart;
    }

    @Description("String to add at the end of each page")
    public String getPageEnd() {
      return pageEnd;
    }

    public void setPageEnd(String pageEnd) {
      this.pageEnd = pageEnd;
    }

    @Description("String to add at the start of each paragraph")
    public String getParagraphStart() {
      return paragraphStart;
    }

    public void setParagraphStart(String paragraphStart) {
      this.paragraphStart = paragraphStart;
    }

    @Description("String to add at the end of each paragraph")
    public String getParagraphEnd() {
      return paragraphEnd;
    }

    public void setParagraphEnd(String paragraphEnd) {
      this.paragraphEnd = paragraphEnd;
    }
  }
}
