/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.drew.lang.GeoLocation;

import io.annot8.common.data.content.FileContent;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.components.responses.ProcessorResponse.Status;
import io.annot8.core.data.Item;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestAnnotationStore;

public class ExifMetadataProcessorTest {

  @Test
  public void testProcess() {
    Item item = Mockito.mock(Item.class);
    FileContent fileContent = Mockito.mock(FileContent.class);
    AnnotationStore store = new TestAnnotationStore();

    URL resource = ExifMetadataProcessorTest.class.getClassLoader().getResource("testimage.jpg");
    File file = null;
    try {
      file = new File(resource.toURI());
    } catch (URISyntaxException e) {
      fail("Error not expected when finding test file");
    }
    when(fileContent.getData()).thenReturn(file);
    when(fileContent.getAnnotations()).thenReturn(store);

    doAnswer(
            new Answer<Stream<FileContent>>() {
              @Override
              public Stream<FileContent> answer(InvocationOnMock invocation) throws Throwable {
                return Stream.of(fileContent);
              }
            })
        .when(item)
        .getContents(Mockito.eq(FileContent.class));

    ExifMetadataProcessor processor = new ExifMetadataProcessor();

    ProcessorResponse response = processor.process(item);

    assertNotNull(response);
    assertEquals(Status.OK, response.getStatus());
    assertEquals(61, store.getAll().count());

    store.getAll().collect(Collectors.toList());

    assertTrue(containsKeyValue(store.getAll(), "Model", "Pixel XL"));
    assertTrue(containsKeyValue(store.getAll(), "Make", "Google"));
    assertTrue(containsKeyValue(store.getAll(), "Model", "Pixel XL"));
    assertTrue(containsKeyValue(store.getAll(), "Image Width", 3036));
    assertTrue(containsKeyValue(store.getAll(), "Image Height", 4048));
    assertTrue(containsKeyValue(store.getAll(), "Date/Time Original", 1537799743000l));

    List<Annotation> geoLocation =
        store
            .getAll()
            .filter(a -> a.getProperties().has("Geo Location"))
            .collect(Collectors.toList());
    List<Annotation> gpsDate =
        store.getAll().filter(a -> a.getProperties().has("Gps Date")).collect(Collectors.toList());

    assertEquals(1, geoLocation.size());
    assertEquals(1, gpsDate.size());

    GeoLocation location =
        (GeoLocation) geoLocation.get(0).getProperties().get("Geo Location").get();
    long gpsDateMillis = (long) gpsDate.get(0).getProperties().get("Gps Date").get();

    assertEquals(51.897819444444444, location.getLatitude());
    assertEquals(-2.0717722222222226, location.getLongitude());

    assertEquals(1537796134000l, gpsDateMillis);
  }

  private boolean containsKeyValue(Stream<Annotation> annotations, String key, Object value) {
    return containsKeyValue(annotations, key, (v) -> value.equals(v));
  }

  private boolean containsKeyValue(
      Stream<Annotation> annotations, String key, Predicate<Object> valueMatches) {
    return annotations
            .map(Annotation::getProperties)
            .filter(p -> p.has(key) && valueMatches.test(p.get(key).get()))
            .count()
        > 0;
  }
}
