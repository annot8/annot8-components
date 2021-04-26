/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.api.properties.Properties;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class HtmlExtractorTest extends AbstractDocumentExtractorTest {
  @Override
  protected Class<? extends AbstractDocumentExtractorDescriptor<?, HtmlExtractor.Settings>>
      getDescriptor() {
    return HtmlExtractor.class;
  }

  @Override
  protected DocumentExtractorSettings getSettings() {
    return new HtmlExtractor.Settings();
  }

  @Override
  protected File getTestFile() {
    URL resource = HtmlExtractorTest.class.getResource("testDocument.html");
    try {
      return Paths.get(resource.toURI()).toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void validateMetadata(Properties itemProperties) {
    assertEquals(
        "Test Document", itemProperties.get(PropertyKeys.PROPERTY_KEY_TITLE).orElse("UNKNOWN"));
    assertEquals("en", itemProperties.get(PropertyKeys.PROPERTY_KEY_LANGUAGE).orElse("UNKNOWN"));

    assertEquals("Joe Bloggs", itemProperties.get(DocumentProperties.AUTHOR).orElse("UNKNOWN"));
    assertEquals(
        List.of("John Doe", "Jane Doe"),
        itemProperties.get(DocumentProperties.CREATOR).orElse(Collections.emptyList()));
    assertEquals(
        List.of("example", "test"),
        itemProperties.get(DocumentProperties.KEYWORDS).orElse(Collections.emptyList()));
    assertTrue(itemProperties.has("viewport"));
  }

  @Override
  protected void validateText(Collection<Text> textContents) {
    assertEquals(1, textContents.size());

    String content = textContents.stream().findFirst().get().getData();

    assertTrue(content.contains("Test Document"));
    assertTrue(content.contains("Bold text"));
    assertTrue(content.contains("Links"));
    assertTrue(content.contains("Royal Air Force Museum"));
  }

  @Override
  protected void validateImages(Collection<Image> imageContents) {
    assertEquals(3, imageContents.size());

    Comparator<Image> sortByIndex =
        (i1, i2) -> {
          Integer index1 =
              i1.getProperties().get(PropertyKeys.PROPERTY_KEY_INDEX, Integer.class).orElse(-1);
          Integer index2 =
              i2.getProperties().get(PropertyKeys.PROPERTY_KEY_INDEX, Integer.class).orElse(-1);
          return Integer.compare(index1, index2);
        };

    List<Image> images = imageContents.stream().sorted(sortByIndex).collect(Collectors.toList());

    Image image1 = images.get(0);

    assertNotNull(image1.getData());
    assertTrue(image1.getData().getWidth() > 0);
    assertTrue(image1.getData().getHeight() > 0);
    assertEquals(1, image1.getProperties().get(PropertyKeys.PROPERTY_KEY_INDEX).get());
    assertEquals(
        "640px-Cosford-_Royal_Air_Force_Museum-_English_Electric_Lightning_suspended_from_the_ceiling_%28geograph_5765866%29.jpg",
        image1.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).get());
    assertEquals(
        "Cosford - Royal Air Force Museum - English Electric Lightning suspended from the ceiling",
        image1.getProperties().get(PropertyKeys.PROPERTY_KEY_TITLE).get());
    assertEquals(
        "Michael Garlick / Cosford: Royal Air Force Museum: English Electric Lightning suspended from the ceiling / CC BY-SA 2.0",
        image1.getProperties().get(PropertyKeys.PROPERTY_KEY_DESCRIPTION).get());
    assertEquals(
        "English Electric Lightning suspended from the ceiling at RAF Museum Cosford",
        image1.getProperties().get("html/alt").get());

    Image image2 = images.get(1);

    assertNotNull(image2.getData());
    assertTrue(image2.getData().getWidth() > 0);
    assertTrue(image2.getData().getHeight() > 0);
    assertEquals(2, image2.getProperties().get(PropertyKeys.PROPERTY_KEY_INDEX).get());
    assertEquals(
        "640px-Imperial_War_Museum_%28geograph_5302500%29.jpg",
        image2.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).get());
    assertFalse(image2.getProperties().has(PropertyKeys.PROPERTY_KEY_TITLE));
    assertFalse(image2.getProperties().has(PropertyKeys.PROPERTY_KEY_DESCRIPTION));

    Image image3 = images.get(2);

    assertNotNull(image3.getData());
    assertTrue(image3.getData().getWidth() > 0);
    assertTrue(image3.getData().getHeight() > 0);
    assertEquals(3, image3.getProperties().get(PropertyKeys.PROPERTY_KEY_INDEX).get());
    assertFalse(image3.getProperties().has(PropertyKeys.PROPERTY_KEY_NAME));
    assertFalse(image3.getProperties().has(PropertyKeys.PROPERTY_KEY_TITLE));
    assertFalse(image3.getProperties().has(PropertyKeys.PROPERTY_KEY_DESCRIPTION));
    assertEquals("Rainbow", image3.getProperties().get("html/alt").get());
  }

  @Override
  protected void validateTables(Collection<TableContent> tableContents) {
    assertEquals(1, tableContents.size());
    TableContent tableContent = tableContents.stream().findFirst().get();

    assertNotNull(tableContent.getData());

    Properties p = tableContent.getProperties();
    assertEquals("en", p.get(PropertyKeys.PROPERTY_KEY_LANGUAGE).get());
    assertEquals("colour_prefs", p.get(PropertyKeys.PROPERTY_KEY_IDENTIFIER).get());
    assertEquals("Example table", p.get(PropertyKeys.PROPERTY_KEY_TITLE).get());
    assertEquals(
        "People and their colour preferences", p.get(PropertyKeys.PROPERTY_KEY_DESCRIPTION).get());

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

  @Test
  public void testCssQuery() {
    Item item = new TestItem();
    item.createContent(FileContent.class).withData(getTestFile()).save();

    HtmlExtractor.Settings s = new HtmlExtractor.Settings();
    s.setExtractText(true);
    s.setExtractImages(false);
    s.setExtractMetadata(false);
    s.setExtractTables(false);
    s.setDiscardOriginal(true);

    s.setCssQueryText("ul");

    HtmlExtractor.Processor p = new HtmlExtractor.Processor(new SimpleContext(), s);

    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(1, item.getContents(Text.class).count());

    Text t = item.getContents(Text.class).findFirst().orElseThrow();
    assertEquals("Lists Bold text Italic text Links", t.getData());
  }

  @Test
  public void testCssQueryMultiple() {
    Item item = new TestItem();
    item.createContent(FileContent.class).withData(getTestFile()).save();

    HtmlExtractor.Settings s = new HtmlExtractor.Settings();
    s.setExtractText(true);
    s.setExtractImages(false);
    s.setExtractMetadata(false);
    s.setExtractTables(false);
    s.setDiscardOriginal(true);

    s.setCssQueryText("ul > li");

    HtmlExtractor.Processor p = new HtmlExtractor.Processor(new SimpleContext(), s);

    assertEquals(ProcessorResponse.ok(), p.process(item));

    assertEquals(4, item.getContents(Text.class).count());
    assertTrue(
        item.getContents(Text.class)
            .allMatch(t -> t.getProperties().has(PropertyKeys.PROPERTY_KEY_INDEX)));

    List<String> contents =
        item.getContents(Text.class).map(Text::getData).collect(Collectors.toList());

    assertTrue(contents.contains("Lists"));
    assertTrue(contents.contains("Bold text"));
    assertTrue(contents.contains("Italic text"));
    assertTrue(contents.contains("Links"));
  }
}
