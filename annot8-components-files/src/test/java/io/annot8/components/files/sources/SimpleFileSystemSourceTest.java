/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.responses.SourceResponse;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItemFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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

  @Test
  public void testOrderingAZ() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));
    settings.setFileOrder(SimpleFileSystemSource.FileOrder.NAME_A_TO_Z);

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(3, tif.getCreatedItems().size());

    assertEquals(
        List.of("file1.txt", "file2.pdf", "file3.txt"),
        tif.getCreatedItems().stream()
            .map(
                i ->
                    i.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SOURCE, Path.class)
                        .get()
                        .getFileName()
                        .toString())
            .collect(Collectors.toList()));

    deleteTestFiles(root);
  }

  @Test
  public void testOrderingZA() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));
    settings.setFileOrder(SimpleFileSystemSource.FileOrder.NAME_Z_TO_A);

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(3, tif.getCreatedItems().size());

    assertEquals(
        List.of("file3.txt", "file2.pdf", "file1.txt"),
        tif.getCreatedItems().stream()
            .map(
                i ->
                    i.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SOURCE, Path.class)
                        .get()
                        .getFileName()
                        .toString())
            .collect(Collectors.toList()));

    deleteTestFiles(root);
  }

  @Test
  public void testOrderingSmallLarge() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));
    settings.setFileOrder(SimpleFileSystemSource.FileOrder.SIZE_SMALL_TO_LARGE);

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(3, tif.getCreatedItems().size());

    assertEquals(
        List.of("file2.pdf", "file1.txt", "file3.txt"),
        tif.getCreatedItems().stream()
            .map(
                i ->
                    i.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SOURCE, Path.class)
                        .get()
                        .getFileName()
                        .toString())
            .collect(Collectors.toList()));

    deleteTestFiles(root);
  }

  @Test
  public void testOrderingLargeSmall() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));
    settings.setFileOrder(SimpleFileSystemSource.FileOrder.SIZE_LARGE_TO_SMALL);

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(3, tif.getCreatedItems().size());

    assertEquals(
        List.of("file3.txt", "file1.txt", "file2.pdf"),
        tif.getCreatedItems().stream()
            .map(
                i ->
                    i.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SOURCE, Path.class)
                        .get()
                        .getFileName()
                        .toString())
            .collect(Collectors.toList()));

    deleteTestFiles(root);
  }

  @Test
  public void testOrderingEarlyLate() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root, true);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));
    settings.setFileOrder(SimpleFileSystemSource.FileOrder.CREATED_DATE_EARLIEST_TO_LATEST);

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(3, tif.getCreatedItems().size());

    assertEquals(
        List.of("file1.txt", "file2.pdf", "file3.txt"),
        tif.getCreatedItems().stream()
            .map(
                i ->
                    i.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SOURCE, Path.class)
                        .get()
                        .getFileName()
                        .toString())
            .collect(Collectors.toList()));

    deleteTestFiles(root);
  }

  @Test
  public void testOrderingLateEarly() throws Exception {
    Path root = Files.createTempDirectory("simplefilesystemsource-");
    createTestFiles(root, true);

    SimpleFileSystemSource.Settings settings = new SimpleFileSystemSource.Settings();
    settings.setPaths(List.of(root));
    settings.setFileOrder(SimpleFileSystemSource.FileOrder.CREATED_DATE_LATEST_TO_EARLIEST);

    SimpleFileSystemSource.Source source = new SimpleFileSystemSource.Source(settings);
    TestItemFactory tif = new TestItemFactory();

    SourceResponse sr = source.read(tif);
    while (sr.getStatus() == SourceResponse.Status.OK) {
      sr = source.read(tif);
    }

    assertEquals(SourceResponse.Status.DONE, sr.getStatus());
    assertEquals(3, tif.getCreatedItems().size());

    assertEquals(
        List.of("file3.txt", "file2.pdf", "file1.txt"),
        tif.getCreatedItems().stream()
            .map(
                i ->
                    i.getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SOURCE, Path.class)
                        .get()
                        .getFileName()
                        .toString())
            .collect(Collectors.toList()));

    deleteTestFiles(root);
  }

  private void createTestFiles(Path p) throws IOException {
    createTestFiles(p, false);
  }

  private void createTestFiles(Path p, boolean delay) throws IOException {
    Path p1 = Files.createFile(p.resolve("file1.txt"));
    Files.writeString(p1, "Hello");

    if (delay)
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }

    Files.createFile(p.resolve("file2.pdf"));

    if (delay)
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }

    Files.createDirectory(p.resolve("folder"));
    Path p3 = Files.createFile(p.resolve("folder").resolve("file3.txt"));
    Files.writeString(p3, "Hello World!");
  }

  private void deleteTestFiles(Path p) throws IOException {
    Files.walk(p).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
