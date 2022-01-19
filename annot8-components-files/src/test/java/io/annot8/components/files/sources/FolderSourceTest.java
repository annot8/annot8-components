/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.responses.SourceResponse;
import io.annot8.api.data.Item;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.TestItemFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class FolderSourceTest {

  @Test
  public void testRecursive() throws Exception {
    Path root = Files.createTempDirectory("foldersource-");
    createTestFiles(root);

    FolderSource.Settings settings = new FolderSource.Settings();
    settings.setPaths(List.of(root));

    try (FolderSource.Source source = new FolderSource.Source(settings)) {
      TestItemFactory tif = new TestItemFactory();

      SourceResponse sr = source.read(tif);
      while (sr.getStatus() == SourceResponse.Status.OK) {
        sr = source.read(tif);
      }

      assertEquals(SourceResponse.Status.DONE, sr.getStatus());
      assertEquals(3, tif.getCreatedItems().size());
      tif.getCreatedItems()
          .forEach(
              item -> {
                assertEquals(
                    2,
                    ((TestItemFactory) ((TestItem) item).getItemFactory())
                        .getCreatedItems()
                        .size());
              });
    } finally {
      deleteTestFiles(root);
    }
  }

  @Test
  public void testNotRecursive() throws Exception {
    Path root = Files.createTempDirectory("foldersource-");
    createTestFiles(root);

    FolderSource.Settings settings = new FolderSource.Settings();
    settings.setPaths(List.of(root));
    settings.setRecursive(false);

    try (FolderSource.Source source = new FolderSource.Source(settings)) {
      TestItemFactory tif = new TestItemFactory();

      SourceResponse sr = source.read(tif);
      while (sr.getStatus() == SourceResponse.Status.OK) {
        sr = source.read(tif);
      }

      assertEquals(SourceResponse.Status.DONE, sr.getStatus());
      assertEquals(2, tif.getCreatedItems().size());
      tif.getCreatedItems()
          .forEach(
              item -> {
                assertEquals(
                    2,
                    ((TestItemFactory) ((TestItem) item).getItemFactory())
                        .getCreatedItems()
                        .size());
              });
    } finally {
      deleteTestFiles(root);
    }
  }

  @Test
  public void testFilterExtensions() throws Exception {
    Path root = Files.createTempDirectory("foldersource-");
    createTestFiles(root);

    FolderSource.Settings settings = new FolderSource.Settings();
    settings.setPaths(List.of(root));
    settings.setExtensions(List.of("TXT"));

    try (FolderSource.Source source = new FolderSource.Source(settings)) {
      TestItemFactory tif = new TestItemFactory();

      SourceResponse sr = source.read(tif);
      while (sr.getStatus() == SourceResponse.Status.OK) {
        sr = source.read(tif);
      }

      assertEquals(SourceResponse.Status.DONE, sr.getStatus());
      assertEquals(3, tif.getCreatedItems().size());
      tif.getCreatedItems()
          .forEach(
              item -> {
                List<Item> createdItems =
                    ((TestItemFactory) ((TestItem) item).getItemFactory()).getCreatedItems();
                assertEquals(1, createdItems.size());
                assertTrue(
                    createdItems
                        .get(0)
                        .getProperties()
                        .get(PropertyKeys.PROPERTY_KEY_SOURCE)
                        .orElseThrow()
                        .toString()
                        .endsWith("txt"));
              });
    } finally {
      deleteTestFiles(root);
    }
  }

  @Test
  public void testRelative() throws Exception {
    Path root = Files.createDirectory(Path.of("./test"));
    createTestFiles(root);

    FolderSource.Settings settings = new FolderSource.Settings();
    settings.setPaths(List.of(root));

    try (FolderSource.Source source = new FolderSource.Source(settings)) {
      TestItemFactory tif = new TestItemFactory();

      SourceResponse sr = source.read(tif);
      while (sr.getStatus() == SourceResponse.Status.OK) {
        sr = source.read(tif);
      }
      assertEquals(SourceResponse.Status.DONE, sr.getStatus());
      assertEquals(3, tif.getCreatedItems().size());
      tif.getCreatedItems()
          .forEach(
              item -> {
                assertEquals(
                    2,
                    ((TestItemFactory) ((TestItem) item).getItemFactory())
                        .getCreatedItems()
                        .size());
              });
    } finally {
      deleteTestFiles(root);
    }
  }

  private void createTestFiles(Path p) throws IOException {
    Path folder1 = Files.createDirectories(p.resolve("f1"));
    Path folder1child = Files.createDirectories(folder1.resolve("child"));
    Path folder2 = Files.createDirectories(p.resolve("f2"));

    addFiles(folder1);
    addFiles(folder1child);
    addFiles(folder2);
  }

  private void addFiles(Path folder) throws IOException {
    Path p1 = Files.createFile(folder.resolve("file1.txt"));
    Files.writeString(p1, "Hello");
    Files.createFile(folder.resolve("file2.pdf"));
  }

  private void deleteTestFiles(Path p) throws IOException {
    Files.walk(p).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }
}
