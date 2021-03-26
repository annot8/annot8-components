/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.properties.Properties;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class PdfExtractorTest extends AbstractDocumentExtractorTest {
  @Override
  protected Class<? extends AbstractDocumentExtractorDescriptor<?, ?>> getDescriptor() {
    return PdfExtractor.class;
  }

  @Override
  protected DocumentExtractorSettings getSettings() {
    return new PdfExtractor.Settings();
  }

  @Override
  protected File getTestFile() {
    URL resource = PdfExtractorTest.class.getResource("testDocument.pdf");
    try {
      return Paths.get(resource.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void validateMetadata(Properties itemProperties) {
    Map<String, Object> props = itemProperties.getAll();

    assertFalse(props.isEmpty());
    assertEquals("Writer", props.get("creator"));
  }

  @Override
  protected void validateText(Collection<Text> textContents) {
    assertEquals(1, textContents.size());
    Text text = textContents.stream().findFirst().get();

    assertTrue(text.getData().contains("Test Document"));
  }

  @Override
  protected void validateImages(Collection<Image> imageContents) {
    assertEquals(1, imageContents.size());
    Image image = imageContents.stream().findFirst().get();

    assertNotNull(image.getData());
    assertTrue(image.getData().getWidth() > 0);
    assertTrue(image.getData().getHeight() > 0);
    assertEquals(1, image.getProperties().get(PropertyKeys.PROPERTY_KEY_INDEX).get());
    assertEquals(2, image.getProperties().get(PropertyKeys.PROPERTY_KEY_PAGE).get());
  }

  @Test
  public void testAdditionalSettings() {
    PdfExtractor.Settings s = new PdfExtractor.Settings();

    assertNotNull(s.getArticleStart());
    s.setArticleStart("ARTICLE START");
    assertEquals("ARTICLE START", s.getArticleStart());

    assertNotNull(s.getArticleEnd());
    s.setArticleEnd("ARTICLE END");
    assertEquals("ARTICLE END", s.getArticleEnd());

    assertNotNull(s.getPageStart());
    s.setPageStart("PAGE START");
    assertEquals("PAGE START", s.getPageStart());

    assertNotNull(s.getPageEnd());
    s.setPageEnd("PAGE END");
    assertEquals("PAGE END", s.getPageEnd());

    assertNotNull(s.getParagraphStart());
    s.setParagraphStart("PARA START");
    assertEquals("PARA START", s.getParagraphStart());

    assertNotNull(s.getParagraphEnd());
    s.setParagraphEnd("PARA END");
    assertEquals("PARA END", s.getParagraphEnd());
  }

  @Override
  protected void validateTables(Collection<TableContent> tableContents) {
    // Do nothing
  }
}
