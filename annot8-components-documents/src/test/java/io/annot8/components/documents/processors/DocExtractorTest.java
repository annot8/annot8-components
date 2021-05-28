/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.properties.Properties;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

public class DocExtractorTest extends AbstractDocumentExtractorTest {
  @Override
  protected Class<? extends AbstractDocumentExtractorDescriptor<?, DocumentExtractorSettings>>
      getDescriptor() {
    return DocExtractor.class;
  }

  @Override
  protected File getTestFile() {
    URL resource = DocExtractorTest.class.getResource("testDocument.doc");
    try {
      return Paths.get(resource.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void validateMetadata(Properties itemProperties) {
    assertFalse(itemProperties.getAll().isEmpty());

    assertEquals("James Baker", itemProperties.get(DocumentProperties.AUTHOR).orElseThrow());
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
    assertEquals("d4.jpg", image.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).get());
  }

  @Override
  protected void validateTables(Collection<TableContent> tableContents) {
    assertEquals(1, tableContents.size());
    TableContent tableContent = tableContents.stream().findFirst().get();

    assertNotNull(tableContent.getData());

    Table table = tableContent.getData();
    assertEquals(3, table.getRowCount());
    assertEquals(3, table.getColumnCount());

    assertEquals(List.of("ID", "Name", "Colour"), table.getColumnNames().get());

    Row r1 = table.getRow(0).get();
    assertEquals("001", r1.getString(0).get());
    assertEquals("Alice", r1.getString(1).get());
    assertEquals("Red", r1.getString(2).get());

    Row r2 = table.getRow(1).get();
    assertEquals("002", r2.getString(0).get());
    assertEquals("Bob", r2.getString(1).get());
    assertEquals("Green", r2.getString(2).get());

    Row r3 = table.getRow(2).get();
    assertEquals("003", r3.getString(0).get());
    assertEquals("Charlie", r3.getString(1).get());
    assertEquals("Blue", r3.getString(2).get());
  }
}
