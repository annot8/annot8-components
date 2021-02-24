/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.responses.SourceResponse;
import io.annot8.testing.testimpl.TestItemFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SimpleFileSystemSourceTest {
  @Test
  public void testRecursive() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(3, tif.getCreatedItems().size());

    deleteTestFiles(root);
  }

  @Test
  public void testNotRecursive() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));
    settings.setRecursive(false);

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(2, tif.getCreatedItems().size());

    deleteTestFiles(root);
  }

  @Test
  public void testFilterExtensions() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));
    settings.setExtensions(List.of("TXT"));

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(2, tif.getCreatedItems().size());

    deleteTestFiles(root);
  }

  @Test
  public void testRelative() throws Exception {
    Path root = Files.createDirectory(Path.of("./test"));
    createTestFiles(root);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(3, tif.getCreatedItems().size());

    deleteTestFiles(root);
  }

  private void createTestFiles(Path p) throws IOException {
    Files.createFile(p.resolve("file1.txt"));
    Files.createFile(p.resolve("file2.pdf"));

    Files.createDirectory(p.resolve("folder"));
    Files.createFile(p.resolve("folder").resolve("file3.txt"));
  }

  private void deleteTestFiles(Path p) throws IOException {
    Files.walk(p).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
