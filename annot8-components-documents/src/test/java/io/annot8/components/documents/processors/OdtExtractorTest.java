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

public class OdtExtractorTest extends AbstractDocumentExtractorTest {
  @Override
  protected Class<? extends AbstractDocumentExtractorDescriptor<?, DocumentExtractorSettings>>
      getDescriptor() {
    return OdtExtractor.class;
  }

  @Override
  protected File getTestFile() {
    URL resource = OdtExtractorTest.class.getResource("testDocument.odt");
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
    assertEquals("James Baker", props.get(DocumentProperties.INITIAL_CREATOR));
    assertEquals(2, props.get(DocumentProperties.PAGE_COUNT));
  }

  @Override
  protected void validateText(Collection<Text> textContents) {
    assertEquals(1, textContents.size());
    Text text = textContents.stream().findFirst().get();

    assertTrue(text.getData().contains("static header"));
    assertTrue(text.getData().contains("Test Document"));
    assertTrue(text.getData().contains("Page"));
  }

  @Override
  protected void validateImages(Collection<Image> imageContents) {
    assertEquals(1, imageContents.size());
    Image image = imageContents.stream().findFirst().get();

    assertNotNull(image.getData());
    assertTrue(image.getData().getWidth() > 0);
    assertTrue(image.getData().getHeight() > 0);
    assertEquals(1, image.getProperties().get(PropertyKeys.PROPERTY_KEY_INDEX).get());
    assertEquals("Picture 2", image.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).get());
    assertFalse(image.getProperties().has(PropertyKeys.PROPERTY_KEY_TITLE));
    assertFalse(image.getProperties().has(PropertyKeys.PROPERTY_KEY_DESCRIPTION));
  }

  @Override
  protected void validateTables(Collection<TableContent> tableContents) {
    // Do nothing
  }
}
