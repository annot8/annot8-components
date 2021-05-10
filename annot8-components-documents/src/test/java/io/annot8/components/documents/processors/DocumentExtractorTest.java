/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class DocumentExtractorTest {
  @Test
  public void testDoc() {
    testInputStream("testDocument.doc", new DocExtractorTest());
    testFile("testDocument.doc", new DocExtractorTest());
  }

  @Test
  public void testDocx() {
    testInputStream("testDocument.docx", new DocxExtractorTest());
    testFile("testDocument.docx", new DocxExtractorTest());
  }

  @Test
  public void testHtml() {
    testInputStream("testDocument.html", new HtmlExtractorTest());
    testFile("testDocument.html", new HtmlExtractorTest());
  }

  @Test
  public void testOdt() {
    testInputStream("testDocument.odt", new OdtExtractorTest());
    testFile("testDocument.odt", new OdtExtractorTest());
  }

  @Test
  public void testPdf() {
    testInputStream("testDocument.pdf", new PdfExtractorTest());
    testFile("testDocument.pdf", new PdfExtractorTest());
  }

  @Test
  public void testPpt() {
    testInputStream("testPresentation.ppt", new PptExtractorTest());
    testFile("testPresentation.ppt", new PptExtractorTest());
  }

  @Test
  public void testPptx() {
    testInputStream("testPresentation.pptx", new PptxExtractorTest());
    testFile("testPresentation.pptx", new PptxExtractorTest());
  }

  @Test
  public void testPlainText() {
    testInputStream("badFile.txt", new PlainTextExtractorTest());
    testFile("badFile.txt", new PlainTextExtractorTest());
  }

  private void testInputStream(String fileName, AbstractDocumentExtractorTest t) {
    DocumentExtractor.Processor p =
        new DocumentExtractor.Processor(new SimpleContext(), new DocumentExtractor.Settings());

    Item item = new TestItem();
    item.createContent(InputStreamContent.class)
        .withData(() -> DocumentExtractorTest.class.getResourceAsStream(fileName))
        .save();

    assertEquals(ProcessorResponse.ok(), p.process(item));

    t.validateText(item.getContents(Text.class).collect(Collectors.toList()));
    t.validateImages(item.getContents(Image.class).collect(Collectors.toList()));
    t.validateTables(item.getContents(TableContent.class).collect(Collectors.toList()));
    t.validateMetadata(item.getProperties());
  }

  private void testFile(String fileName, AbstractDocumentExtractorTest t) {
    DocumentExtractor.Processor p =
        new DocumentExtractor.Processor(new SimpleContext(), new DocumentExtractor.Settings());

    URL resource = DocumentExtractorTest.class.getResource(fileName);
    File file;
    try {
      file = Paths.get(resource.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    Item item = new TestItem();
    item.createContent(FileContent.class).withData(file).save();

    assertEquals(ProcessorResponse.ok(), p.process(item));

    t.validateText(item.getContents(Text.class).collect(Collectors.toList()));
    t.validateImages(item.getContents(Image.class).collect(Collectors.toList()));
    t.validateTables(item.getContents(TableContent.class).collect(Collectors.toList()));
    t.validateMetadata(item.getProperties());
  }
}
