/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.annot8.common.data.content.FileContent;
import io.annot8.conventions.FileMetadataKeys;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.components.responses.ProcessorResponse.Status;
import io.annot8.core.data.Item;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestAnnotationStore;

public class FileMetadataExtractorTest {

  @Test
  public void testProcess() {
    Item item = Mockito.mock(Item.class);
    AnnotationStore store = new TestAnnotationStore();

    FileContent fileContent = getFileContent("testfilemetadata.txt");
    File file = fileContent.getData();

    doAnswer(getStreamAnswer(fileContent)).when(item).getContents(eq(FileContent.class));

    FileMetadataExtractor extractor = new FileMetadataExtractor();

    ProcessorResponse processResponse = extractor.process(item);

    assertEquals(Status.OK, processResponse.getStatus());

    List<Annotation> annotations =
        fileContent.getAnnotations().getAll().collect(Collectors.toList());
    assertEquals(file.getAbsolutePath(), getKeyValue(annotations, FileMetadataKeys.PATH));
    assertEquals("txt", getKeyValue(annotations, FileMetadataKeys.EXTENSION));
    assertFalse((boolean) getKeyValue(annotations, FileMetadataKeys.HIDDEN));
    assertTrue((boolean) getKeyValue(annotations, FileMetadataKeys.REGULAR));
    assertFalse((boolean) getKeyValue(annotations, FileMetadataKeys.SYM_LINK));
    assertNotNull(getKeyValue(annotations, FileMetadataKeys.DATE_CREATED));
    assertNotNull(getKeyValue(annotations, FileMetadataKeys.LAST_MODIFIED));
    assertNotNull(getKeyValue(annotations, FileMetadataKeys.LAST_ACCESS_DATE));
    assertEquals(60L, getKeyValue(annotations, FileMetadataKeys.FILE_SIZE));
    assertNotNull(getKeyValue(annotations, FileMetadataKeys.OWNER));
    assertFalse((boolean) getKeyValue(annotations, FileMetadataKeys.DIRECTORY));
    annotations.forEach(a -> assertEquals(FileMetadataExtractor.FILE_METADATA, a.getType()));
  }

  @Test
  public void testProcessNoFileContent() {
    Item item = Mockito.mock(Item.class);
    when(item.getContents(eq(FileContent.class))).thenReturn(Stream.empty());

    FileMetadataExtractor extractor = new FileMetadataExtractor();
    ProcessorResponse processResponse = extractor.process(item);

    assertEquals(Status.OK, processResponse.getStatus());
  }

  @Test
  public void testProcessNoFileExtension() {
    Item item = Mockito.mock(Item.class);
    FileContent content = getFileContent("noExtension");
    doAnswer(getStreamAnswer(content)).when(item).getContents(eq(FileContent.class));
    FileMetadataExtractor extractor = new FileMetadataExtractor();
    ProcessorResponse response = extractor.process(item);
    assertEquals(Status.OK, response.getStatus());

    List<Annotation> annotations = content.getAnnotations().getAll().collect(Collectors.toList());
    for (Annotation annotation : annotations) {
      if (annotation.getProperties().has(FileMetadataKeys.EXTENSION)) {
        fail("No annotation with a file extension property is expected");
      }
    }
  }

  @Test
  public void testFileNotExisting() {
    Item item = Mockito.mock(Item.class);
    FileContent fileContent = Mockito.mock(FileContent.class);
    when(fileContent.getData()).thenReturn(new File("nonExistentFile"));
    doAnswer(
            new Answer<Stream<FileContent>>() {
              @Override
              public Stream<FileContent> answer(InvocationOnMock invocation) {
                return Stream.of(fileContent);
              }
            })
        .when(item)
        .getContents(eq(FileContent.class));

    FileMetadataExtractor extractor = new FileMetadataExtractor();
    ProcessorResponse response = extractor.process(item);

    assertEquals(Status.ITEM_ERROR, response.getStatus());
  }

  private FileContent getFileContent(String fileName) {
    AnnotationStore store = new TestAnnotationStore();
    FileContent fileContent = Mockito.mock(FileContent.class);
    URL resource = FileMetadataExtractorTest.class.getResource(fileName);
    File file = null;
    try {
      file = new File(resource.toURI());
    } catch (URISyntaxException e) {
      fail("Error not expected when finding test file");
    }
    when(fileContent.getData()).thenReturn(file);
    when(fileContent.getAnnotations()).thenReturn(store);
    return fileContent;
  }

  private Object getKeyValue(List<Annotation> annotations, String key) {
    for (Annotation annotation : annotations) {
      if (annotation.getProperties().has(key)) {
        return annotation.getProperties().get(key).get();
      }
    }
    fail("Key: " + key + " not found in the provided list");
    return null;
  }

  private <T> Answer<Stream<T>> getStreamAnswer(T... contents) {
    return new Answer<Stream<T>>() {
      @Override
      public Stream<T> answer(InvocationOnMock invocation) throws Throwable {
        return Stream.of(contents);
      }
    };
  }
}
