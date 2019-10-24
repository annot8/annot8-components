/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.components.responses.ProcessorResponse.Status;
import io.annot8.api.data.Item;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.content.FileContent;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestAnnotationStore;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class ExifMetadataProcessorTest {

  @Test
  public void testProcess() {
    Item item = Mockito.mock(Item.class);
    FileContent fileContent = Mockito.mock(FileContent.class);
    AnnotationStore store = new TestAnnotationStore(fileContent);

    URL resource = ExifMetadataProcessorTest.class.getClassLoader().getResource("testimage.jpg");
    File file = null;
    try {
      file = new File(resource.toURI());
    } catch (URISyntaxException e) {
      fail("Error not expected when finding test file");
    }
    when(fileContent.getData()).thenReturn(file);
    when(fileContent.getAnnotations()).thenReturn(store);

    doAnswer((Answer<Stream<FileContent>>) invocation -> Stream.of(fileContent))
        .when(item)
        .getContents(Mockito.eq(FileContent.class));

    Processor processor = new ExifMetadataProcessor.Processor();

    ProcessorResponse response = processor.process(item);

    assertNotNull(response);
    assertEquals(Status.OK, response.getStatus());
    assertEquals(4, store.getByType(ExifMetadataProcessor.EXIF_TYPE).count());
    assertEquals(1, store.getByType(ExifMetadataProcessor.EXIF_GPS_TYPE).count());

    Map<String, Object> allProperties = new HashMap<>();
    store
        .getByType(ExifMetadataProcessor.EXIF_TYPE)
        .forEach(a -> allProperties.putAll(a.getProperties().getAll()));

    containsKeyValue(allProperties, "Make", "Google");
    containsKeyValue(allProperties, "Model", "Pixel XL");
    containsKeyValue(allProperties, "Exif Image Width", 3036);
    containsKeyValue(allProperties, "Exif Image Height", 4048);
    containsKeyValue(allProperties, "Date/Time Original", 1537799743000L);

    Annotation geolocation = store.getByType(ExifMetadataProcessor.EXIF_GPS_TYPE).findFirst().get();
    assertEquals(3, geolocation.getProperties().keys().count());

    assertEquals(
        51.897819444444444,
        geolocation.getProperties().get(PropertyKeys.PROPERTY_KEY_LATITUDE, Double.class).get());
    assertEquals(
        -2.0717722222222226,
        geolocation.getProperties().get(PropertyKeys.PROPERTY_KEY_LONGITUDE, Double.class).get());
    assertEquals(
        1537796134000L,
        geolocation.getProperties().get(PropertyKeys.PROPERTY_KEY_DATE, Long.class).get());
  }

  private void containsKeyValue(Map<String, Object> map, String key, Object value) {
    assertTrue(map.containsKey(key));
    assertEquals(value, map.get(key));
  }
}
