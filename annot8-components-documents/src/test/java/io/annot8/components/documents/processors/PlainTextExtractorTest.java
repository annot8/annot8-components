/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.properties.Properties;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;

public class PlainTextExtractorTest extends AbstractDocumentExtractorTest {

  @Override
  protected Class<? extends AbstractDocumentExtractorDescriptor<?, DocumentExtractorSettings>>
      getDescriptor() {
    return PlainTextExtractor.class;
  }

  @Override
  protected DocumentExtractorSettings getSettings() {
    return new DocumentExtractorSettings();
  }

  @Override
  protected File getTestFile() {
    URL resource = PlainTextExtractorTest.class.getResource("testDocument.html");
    try {
      return Paths.get(resource.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void validateMetadata(Properties itemProperties) {
    // Do nothing
  }

  @Override
  protected void validateText(Collection<Text> textContents) {
    assertEquals(1, textContents.size());
  }

  @Override
  protected void validateImages(Collection<Image> imageContents) {
    // Do nothing
  }

  @Override
  protected void validateTables(Collection<TableContent> tableContents) {
    // Do nothing
  }

  @Override
  public void testBadFile() {
    // Override this, as this processor will handle any type
  }

  @Override
  public void testBadInputStream() {
    // Override this, as this processor will handle any type
  }
}
