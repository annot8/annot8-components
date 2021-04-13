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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.poifs.filesystem.FileMagic;

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
      Collection<ExtractionWithProperties<BufferedImage>> images = new ArrayList<>();

      int imageNumber = 0;
      Collection<COSName> topLevelImages = new ArrayList<>();

      COSDictionary topLevelResources =
          doc.getPages().getCOSObject().getCOSDictionary(COSName.RESOURCES);
      if (topLevelResources != null) {
        PDResources res = new PDResources(topLevelResources);

        Collection<ExtractionWithProperties<BufferedImage>> extracted =
            getImagesFromResources(res, null, imageNumber, Collections.emptyList());
        imageNumber += extracted.size();

        extracted.forEach(
            e -> {
              String name = e.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).toString();
              if (name != null) topLevelImages.add(COSName.getPDFName(name));
            });

        images.addAll(extracted);
      }

      int pageNumber = 0;
      for (PDPage page : doc.getPages()) {
        pageNumber++;

        PDResources pdResources = page.getResources();
        Collection<ExtractionWithProperties<BufferedImage>> extracted =
            getImagesFromResources(pdResources, pageNumber, imageNumber, topLevelImages);
        imageNumber += extracted.size();

        images.addAll(extracted);
      }

      return images;
    }

    private Collection<ExtractionWithProperties<BufferedImage>> getImagesFromResources(
        PDResources resources, Integer pageNumber, int startingIndex, Collection<COSName> skip) {
      List<ExtractionWithProperties<BufferedImage>> images = new ArrayList<>();
      int imageNumber = startingIndex;

      for (COSName name : resources.getXObjectNames()) {
        if (skip.contains(name)) continue;

        try {
          PDXObject o = resources.getXObject(name);
          if (o instanceof PDImageXObject) {
            PDImageXObject image = (PDImageXObject) o;
            imageNumber++;

            Map<String, Object> imageMetadata = new HashMap<>();
            PDMetadata metadata = image.getMetadata();

            // Extract metadata
            if (metadata != null) {
              Metadata md = new Metadata();
              XmpReader xmpReader = new XmpReader();

              xmpReader.extract(metadata.toByteArray(), md);
              imageMetadata.putAll(toMap(md));
            }

            imageMetadata.put(PropertyKeys.PROPERTY_KEY_NAME, name.getName());

            // Note that index numbers are the order the images are extracted, and not necessarily
            // the order in which they appear in the document
            imageMetadata.put(PropertyKeys.PROPERTY_KEY_INDEX, imageNumber);

            if (pageNumber != null) imageMetadata.put(PropertyKeys.PROPERTY_KEY_PAGE, pageNumber);

            images.add(new ExtractionWithProperties<>(image.getImage(), imageMetadata));
          }
        } catch (IOException e) {
          log().warn("Unable to read resource {}", name.getName(), e);
        }
      }

      return images;
    }

    @Override
    public Collection<ExtractionWithProperties<Table>> extractTables(PDDocument doc)
        throws ProcessingException {
      // TODO: Extract tables from PDF
      return Collections.emptyList();
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
