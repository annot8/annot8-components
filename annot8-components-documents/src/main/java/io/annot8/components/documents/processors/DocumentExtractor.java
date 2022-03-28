/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.api.settings.Description;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Table;
import io.annot8.components.documents.data.ExtractionWithProperties;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.nodes.Document;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

@ComponentName("Document Extractor")
@ComponentDescription(
    "Extracts images, tables, metadata and text from Document files by delegating to type-specific extractors")
@ComponentTags({
  "documents",
  "extractor",
  "text",
  "images",
  "tables",
  "metadata",
  "doc",
  "docx",
  "html",
  "odt",
  "pdf",
  "ppt",
  "pptx"
})
@SettingsClass(DocumentExtractor.Settings.class)
public class DocumentExtractor
    extends AbstractDocumentExtractorDescriptor<
        DocumentExtractor.Processor, DocumentExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractor.Settings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<
          DocumentObjectWithType, DocumentExtractor.Settings> {

    private final DocExtractor.Processor docProcessor;
    private final DocxExtractor.Processor docxProcessor;
    private final HtmlExtractor.Processor htmlProcessor;
    private final OdtExtractor.Processor odtProcessor;
    private final PdfExtractor.Processor pdfProcessor;
    private final PptExtractor.Processor pptProcessor;
    private final PptxExtractor.Processor pptxProcessor;
    private final PlainTextExtractor.Processor plainTextProcessor;

    private final Map<String, DocumentType> contentToType = new HashMap<>();

    public Processor(Context context, DocumentExtractor.Settings settings) {
      super(context, settings);

      docProcessor = new DocExtractor.Processor(context, new DocumentExtractorSettings(settings));
      docxProcessor = new DocxExtractor.Processor(context, new DocumentExtractorSettings(settings));
      htmlProcessor = new HtmlExtractor.Processor(context, new HtmlExtractor.Settings(settings));
      odtProcessor = new OdtExtractor.Processor(context, new DocumentExtractorSettings(settings));
      pdfProcessor = new PdfExtractor.Processor(context, new PdfExtractor.Settings(settings));
      pptProcessor = new PptExtractor.Processor(context, new DocumentExtractorSettings(settings));
      pptxProcessor = new PptxExtractor.Processor(context, new DocumentExtractorSettings(settings));
      plainTextProcessor =
          new PlainTextExtractor.Processor(context, new DocumentExtractorSettings(settings));
    }

    @Override
    public void reset() {
      contentToType.clear();

      docProcessor.reset();
      docxProcessor.reset();
      htmlProcessor.reset();
      odtProcessor.reset();
      pdfProcessor.reset();
      pptProcessor.reset();
      pptxProcessor.reset();
      plainTextProcessor.reset();
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
      DocumentType documentType = DocumentType.PLAIN_TEXT; // Accepts any document, so use it as our
      // default

      if (docProcessor.acceptFile(file)) {
        documentType = DocumentType.DOC;
      } else if (docxProcessor.acceptFile(file)) {
        documentType = DocumentType.DOCX;
      } else if (htmlProcessor.acceptFile(file)) {
        documentType = DocumentType.HTML;
      } else if (odtProcessor.acceptFile(file)) {
        documentType = DocumentType.ODT;
      } else if (pdfProcessor.acceptFile(file)) {
        documentType = DocumentType.PDF;
      } else if (pptProcessor.acceptFile(file)) {
        documentType = DocumentType.PPT;
      } else if (pptxProcessor.acceptFile(file)) {
        documentType = DocumentType.PPTX;
      } else if (!settings.isExtractPlainText()) {
        return false;
      }

      contentToType.put(file.getId(), documentType);
      return true;
    }

    @Override
    protected boolean acceptInputStream(InputStreamContent inputStream) {
      DocumentType documentType = DocumentType.PLAIN_TEXT; // Accepts any document, so use it as our
      // default

      if (docProcessor.acceptInputStream(inputStream)) {
        documentType = DocumentType.DOC;
      } else if (docxProcessor.acceptInputStream(inputStream)) {
        documentType = DocumentType.DOCX;
      } else if (htmlProcessor.acceptInputStream(inputStream)) {
        documentType = DocumentType.HTML;
      } else if (odtProcessor.acceptInputStream(inputStream)) {
        documentType = DocumentType.ODT;
      } else if (pdfProcessor.acceptInputStream(inputStream)) {
        documentType = DocumentType.PDF;
      } else if (pptProcessor.acceptInputStream(inputStream)) {
        documentType = DocumentType.PPT;
      } else if (pptxProcessor.acceptInputStream(inputStream)) {
        documentType = DocumentType.PPTX;
      } else if (!settings.isExtractPlainText()) {
        return false;
      }

      contentToType.put(inputStream.getId(), documentType);
      return true;
    }

    @Override
    protected DocumentObjectWithType extractDocument(FileContent file) throws IOException {
      DocumentType type = contentToType.get(file.getId());
      if (type == null) throw new ProcessingException("FileContent type has not been recorded");

      switch (type) {
        case DOC:
          return new DocumentObjectWithType(docProcessor.extractDocument(file), type);
        case DOCX:
          return new DocumentObjectWithType(docxProcessor.extractDocument(file), type);
        case HTML:
          return new DocumentObjectWithType(htmlProcessor.extractDocument(file), type);
        case ODT:
          return new DocumentObjectWithType(odtProcessor.extractDocument(file), type);
        case PDF:
          return new DocumentObjectWithType(pdfProcessor.extractDocument(file), type);
        case PPT:
          return new DocumentObjectWithType(pptProcessor.extractDocument(file), type);
        case PPTX:
          return new DocumentObjectWithType(pptxProcessor.extractDocument(file), type);
        case PLAIN_TEXT:
          return new DocumentObjectWithType(plainTextProcessor.extractDocument(file), type);
      }

      throw new ProcessingException("Unsupported type " + type);
    }

    @Override
    protected DocumentObjectWithType extractDocument(InputStreamContent inputStreamContent)
        throws IOException {
      DocumentType type = contentToType.get(inputStreamContent.getId());
      if (type == null)
        throw new ProcessingException("InputStreamContent type has not been recorded");

      switch (type) {
        case DOC:
          return new DocumentObjectWithType(docProcessor.extractDocument(inputStreamContent), type);
        case DOCX:
          return new DocumentObjectWithType(
              docxProcessor.extractDocument(inputStreamContent), type);
        case HTML:
          return new DocumentObjectWithType(
              htmlProcessor.extractDocument(inputStreamContent), type);
        case ODT:
          return new DocumentObjectWithType(odtProcessor.extractDocument(inputStreamContent), type);
        case PDF:
          return new DocumentObjectWithType(pdfProcessor.extractDocument(inputStreamContent), type);
        case PPT:
          return new DocumentObjectWithType(pptProcessor.extractDocument(inputStreamContent), type);
        case PPTX:
          return new DocumentObjectWithType(
              pptxProcessor.extractDocument(inputStreamContent), type);
        case PLAIN_TEXT:
          return new DocumentObjectWithType(
              plainTextProcessor.extractDocument(inputStreamContent), type);
      }

      throw new ProcessingException("Unsupported type " + type);
    }

    @Override
    protected Map<String, Object> extractMetadata(DocumentObjectWithType doc) {
      switch (doc.getType()) {
        case DOC:
          return docProcessor.extractMetadata((HWPFDocument) doc.getDocument());
        case DOCX:
          return docxProcessor.extractMetadata((XWPFDocument) doc.getDocument());
        case HTML:
          return htmlProcessor.extractMetadata((Document) doc.getDocument());
        case ODT:
          return odtProcessor.extractMetadata((OdfTextDocument) doc.getDocument());
        case PDF:
          return pdfProcessor.extractMetadata((PDDocument) doc.getDocument());
        case PPT:
          return pptProcessor.extractMetadata((HSLFSlideShow) doc.getDocument());
        case PPTX:
          return pptxProcessor.extractMetadata((XMLSlideShow) doc.getDocument());
        case PLAIN_TEXT:
          return plainTextProcessor.extractMetadata((String) doc.getDocument());
      }

      return Collections.emptyMap();
    }

    @Override
    protected Collection<ExtractionWithProperties<String>> extractText(DocumentObjectWithType doc) {
      switch (doc.getType()) {
        case DOC:
          return docProcessor.extractText((HWPFDocument) doc.getDocument());
        case DOCX:
          return docxProcessor.extractText((XWPFDocument) doc.getDocument());
        case HTML:
          return htmlProcessor.extractText((Document) doc.getDocument());
        case ODT:
          return odtProcessor.extractText((OdfTextDocument) doc.getDocument());
        case PDF:
          return pdfProcessor.extractText((PDDocument) doc.getDocument());
        case PPT:
          return pptProcessor.extractText((HSLFSlideShow) doc.getDocument());
        case PPTX:
          return pptxProcessor.extractText((XMLSlideShow) doc.getDocument());
        case PLAIN_TEXT:
          return plainTextProcessor.extractText((String) doc.getDocument());
      }

      return Collections.emptyList();
    }

    @Override
    protected Collection<ExtractionWithProperties<BufferedImage>> extractImages(
        DocumentObjectWithType doc) {
      switch (doc.getType()) {
        case DOC:
          return docProcessor.extractImages((HWPFDocument) doc.getDocument());
        case DOCX:
          return docxProcessor.extractImages((XWPFDocument) doc.getDocument());
        case HTML:
          return htmlProcessor.extractImages((Document) doc.getDocument());
        case ODT:
          return odtProcessor.extractImages((OdfTextDocument) doc.getDocument());
        case PDF:
          return pdfProcessor.extractImages((PDDocument) doc.getDocument());
        case PPT:
          return pptProcessor.extractImages((HSLFSlideShow) doc.getDocument());
        case PPTX:
          return pptxProcessor.extractImages((XMLSlideShow) doc.getDocument());
        case PLAIN_TEXT:
          return plainTextProcessor.extractImages((String) doc.getDocument());
      }

      return Collections.emptyList();
    }

    @Override
    protected Collection<ExtractionWithProperties<Table>> extractTables(DocumentObjectWithType doc)
        throws ProcessingException {

      switch (doc.getType()) {
        case DOC:
          return docProcessor.extractTables((HWPFDocument) doc.getDocument());
        case DOCX:
          return docxProcessor.extractTables((XWPFDocument) doc.getDocument());
        case HTML:
          return htmlProcessor.extractTables((Document) doc.getDocument());
        case ODT:
          return odtProcessor.extractTables((OdfTextDocument) doc.getDocument());
        case PDF:
          return pdfProcessor.extractTables((PDDocument) doc.getDocument());
        case PPT:
          return pptProcessor.extractTables((HSLFSlideShow) doc.getDocument());
        case PPTX:
          return pptxProcessor.extractTables((XMLSlideShow) doc.getDocument());
        case PLAIN_TEXT:
          return plainTextProcessor.extractTables((String) doc.getDocument());
      }

      return Collections.emptyList();
    }
  }

  public static class Settings extends DocumentExtractorSettings {
    private boolean extractPlainText = false;

    @Description(
        "If true, then any files that can't be extracted via a different processor will be extracted as plain text")
    public boolean isExtractPlainText() {
      return extractPlainText;
    }

    public void setExtractPlainText(boolean extractPlainText) {
      this.extractPlainText = extractPlainText;
    }
  }

  public static class DocumentObjectWithType {
    private final Object document;
    private final DocumentType type;

    private DocumentObjectWithType(Object document, DocumentType type) {
      this.document = document;
      this.type = type;
    }

    public Object getDocument() {
      return document;
    }

    public DocumentType getType() {
      return type;
    }
  }

  public enum DocumentType {
    DOC,
    DOCX,
    HTML,
    ODT,
    PDF,
    PPT,
    PPTX,
    PLAIN_TEXT
  }
}
