/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

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
@SettingsClass(DocumentExtractorSettings.class)
public class DocumentExtractor
    extends AbstractDocumentExtractorDescriptor<
        DocumentExtractor.Processor, DocumentExtractorSettings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<
          DocumentObjectWithType, DocumentExtractorSettings> {

    private final DocExtractor.Processor docProcessor;
    private final DocxExtractor.Processor docxProcessor;
    private final HtmlExtractor.Processor htmlProcessor;
    private final OdtExtractor.Processor odtProcessor;
    private final PdfExtractor.Processor pdfProcessor;
    private final PptExtractor.Processor pptProcessor;
    private final PptxExtractor.Processor pptxProcessor;

    private final Map<String, DocumentType> contentToType = new HashMap<>();

    public Processor(Context context, DocumentExtractorSettings settings) {
      super(context, settings);

      docProcessor = new DocExtractor.Processor(context, new DocumentExtractorSettings(settings));
      docxProcessor = new DocxExtractor.Processor(context, new DocumentExtractorSettings(settings));
      htmlProcessor = new HtmlExtractor.Processor(context, new HtmlExtractor.Settings(settings));
      odtProcessor = new OdtExtractor.Processor(context, new DocumentExtractorSettings(settings));
      pdfProcessor = new PdfExtractor.Processor(context, new PdfExtractor.Settings(settings));
      pptProcessor = new PptExtractor.Processor(context, new DocumentExtractorSettings(settings));
      pptxProcessor = new PptxExtractor.Processor(context, new DocumentExtractorSettings(settings));

      // TODO: Should we add in Excel?
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
      return true;
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
    }

    @Override
    public boolean acceptFile(FileContent file) {
      if (docProcessor.acceptFile(file)) {
        contentToType.put(file.getId(), DocumentType.DOC);
        return true;
      }

      if (docxProcessor.acceptFile(file)) {
        contentToType.put(file.getId(), DocumentType.DOCX);
        return true;
      }

      if (htmlProcessor.acceptFile(file)) {
        contentToType.put(file.getId(), DocumentType.HTML);
        return true;
      }

      if (odtProcessor.acceptFile(file)) {
        contentToType.put(file.getId(), DocumentType.ODT);
        return true;
      }

      if (pdfProcessor.acceptFile(file)) {
        contentToType.put(file.getId(), DocumentType.PDF);
        return true;
      }

      if (pptProcessor.acceptFile(file)) {
        contentToType.put(file.getId(), DocumentType.PPT);
        return true;
      }

      if (pptxProcessor.acceptFile(file)) {
        contentToType.put(file.getId(), DocumentType.PPTX);
        return true;
      }

      return false;
    }

    @Override
    public boolean acceptInputStream(InputStreamContent inputStream) {
      if (docProcessor.acceptInputStream(inputStream)) {
        contentToType.put(inputStream.getId(), DocumentType.DOC);
        return true;
      }

      if (docxProcessor.acceptInputStream(inputStream)) {
        contentToType.put(inputStream.getId(), DocumentType.DOCX);
        return true;
      }

      if (htmlProcessor.acceptInputStream(inputStream)) {
        contentToType.put(inputStream.getId(), DocumentType.HTML);
        return true;
      }

      if (odtProcessor.acceptInputStream(inputStream)) {
        contentToType.put(inputStream.getId(), DocumentType.ODT);
        return true;
      }

      if (pdfProcessor.acceptInputStream(inputStream)) {
        contentToType.put(inputStream.getId(), DocumentType.PDF);
        return true;
      }

      if (pptProcessor.acceptInputStream(inputStream)) {
        contentToType.put(inputStream.getId(), DocumentType.PPT);
        return true;
      }

      if (pptxProcessor.acceptInputStream(inputStream)) {
        contentToType.put(inputStream.getId(), DocumentType.PPTX);
        return true;
      }

      return false;
    }

    @Override
    public DocumentObjectWithType extractDocument(FileContent file) throws IOException {
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
      }

      throw new ProcessingException("Unsupported type " + type);
    }

    @Override
    public DocumentObjectWithType extractDocument(InputStreamContent inputStreamContent)
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
      }

      throw new ProcessingException("Unsupported type " + type);
    }

    @Override
    public Map<String, Object> extractMetadata(DocumentObjectWithType doc) {
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
      }

      return Collections.emptyMap();
    }

    @Override
    public Collection<ExtractionWithProperties<String>> extractText(DocumentObjectWithType doc) {
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
      }

      return Collections.emptyList();
    }

    @Override
    public Collection<ExtractionWithProperties<BufferedImage>> extractImages(
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
      }

      return Collections.emptyList();
    }

    @Override
    public Collection<ExtractionWithProperties<Table>> extractTables(DocumentObjectWithType doc)
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
      }

      return Collections.emptyList();
    }
  }

  protected static class DocumentObjectWithType {
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

  protected enum DocumentType {
    DOC,
    DOCX,
    HTML,
    ODT,
    PDF,
    PPT,
    PPTX
  }
}
