/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.processors;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import org.mockito.Mockito;

import io.annot8.common.data.content.ColumnMetadata;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.TableMetadata;
import io.annot8.core.stores.AnnotationStore;
import io.annot8.testing.testimpl.TestAnnotationStore;

public abstract class AbstractSQLiteDataTest {

  protected FileContent mockFileContent(String resourceFileName) {
    FileContent fileContent = Mockito.mock(FileContent.class);
    AnnotationStore store = new TestAnnotationStore();
    File file = getTestFile(resourceFileName);
    when(fileContent.getId()).thenReturn("testContentId");
    when(fileContent.getData()).thenReturn(file);
    when(fileContent.getAnnotations()).thenReturn(store);
    return fileContent;
  }

  protected TableMetadata getTestDBMetadata() {
    ColumnMetadata testColumn = new ColumnMetadata("test", 0);
    ColumnMetadata idColumn = new ColumnMetadata("id", 0);
    ColumnMetadata someValue = new ColumnMetadata("someValue", 0);
    return new TableMetadata("test", "TABLE", Arrays.asList(testColumn, idColumn, someValue), 2);
  }

  protected JDBCSettings getTestDBSettings() {
    File testdb = getTestFile("test.db");
    return new JDBCSettings("jdbc:sqlite:/" + testdb.getAbsolutePath());
  }

  private File getTestFile(String resourceFileName) {
    URL resource = AbstractSQLiteDataTest.class.getClassLoader().getResource(resourceFileName);
    try {
      return new File(resource.toURI());
    } catch (URISyntaxException e) {
      fail("Error not expected when finding test file");
    }
    return null;
  }
}
