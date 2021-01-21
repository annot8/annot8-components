/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.properties.Properties;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PptxExtractorTest extends AbstractDocumentExtractorTest {
  @Override
  protected Class<? extends AbstractDocumentExtractorDescriptor<?>> getDescriptor() {
    return PptxExtractor.class;
  }

  @Override
  protected File getTestFile() {
    URL resource = PptxExtractorTest.class.getResource("testPresentation.pptx");
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
    assertEquals("James Baker", props.get(DocumentProperties.CREATOR));
    assertEquals(2, props.get(DocumentProperties.SLIDE_COUNT));
  }

  @Override
  protected void validateText(Collection<Text> textContents) {
    assertEquals(4, textContents.size()); // 2 slides, 1 notes, 1 comment

    List<Text> slides =
        textContents.stream()
            .filter(
                t ->
                    t.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class)
                        .orElse("UNKNOWN")
                        .equals("slide"))
            .collect(Collectors.toList());
    assertEquals(2, slides.size());

    slides.sort(
        Comparator.comparingInt(
            t -> t.getProperties().get(PropertyKeys.PROPERTY_KEY_PAGE, Integer.class).orElse(-1)));

    Text slide1 = slides.get(0);
    assertTrue(slide1.getData().contains("Test Document"));
    assertTrue(slide1.getData().contains("multiple slides"));
    assertEquals(1, slide1.getProperties().get(PropertyKeys.PROPERTY_KEY_PAGE).orElse(-1));
    assertEquals(
        "Slide1", slide1.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).orElse("UNKNOWN"));

    Text slide2 = slides.get(1);
    assertTrue(slide2.getData().contains("Cathedral"));
    assertEquals(2, slide2.getProperties().get(PropertyKeys.PROPERTY_KEY_PAGE).orElse(-1));
    assertEquals(
        "Slide2", slide2.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).orElse("UNKNOWN"));

    List<Text> notes =
        textContents.stream()
            .filter(
                t ->
                    t.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class)
                        .orElse("UNKNOWN")
                        .equals("note"))
            .collect(Collectors.toList());
    assertEquals(1, notes.size());
    Text note = notes.get(0);
    assertTrue(note.getData().contains("Test Note"));
    assertEquals(1, note.getProperties().get(PropertyKeys.PROPERTY_KEY_PAGE).orElse(-1));

    List<Text> comments =
        textContents.stream()
            .filter(
                t ->
                    t.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SUBTYPE, String.class)
                        .orElse("UNKNOWN")
                        .equals("comment"))
            .collect(Collectors.toList());
    assertEquals(1, comments.size());
    Text comment = comments.get(0);
    assertTrue(comment.getData().contains("retrieved on"));
    assertEquals(2, comment.getProperties().get(PropertyKeys.PROPERTY_KEY_PAGE).orElse(-1));
  }

  @Override
  protected void validateImages(Collection<Image> imageContents) {
    assertEquals(1, imageContents.size());

    Image image = imageContents.stream().findFirst().get();

    assertNotNull(image.getData());
    assertTrue(image.getData().getWidth() > 0);
    assertTrue(image.getData().getHeight() > 0);
    assertEquals(1, image.getProperties().get(PropertyKeys.PROPERTY_KEY_INDEX).get());
    assertEquals("image/tiff", image.getProperties().get(PropertyKeys.PROPERTY_KEY_MIMETYPE).get());
    assertEquals("image1.tiff", image.getProperties().get(PropertyKeys.PROPERTY_KEY_NAME).get());
  }
}
