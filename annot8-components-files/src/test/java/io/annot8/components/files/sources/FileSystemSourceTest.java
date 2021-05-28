/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

public class FileSystemSourceTest {

  @Test
  public void testAcceptFile() {
    FileSystemSource.Settings s = new FileSystemSource.Settings();
    s.setAcceptedFileNamePatterns(Set.of(Pattern.compile(".*\\.jpg"), Pattern.compile("[123].*")));
    s.setNegateAcceptedFileNamePatterns(false);

    assertTrue(FileSystemSource.Source.acceptFile(Path.of("foobar.jpg"), s));
    assertTrue(FileSystemSource.Source.acceptFile(Path.of("/test/foobar.jpg"), s));
    assertTrue(FileSystemSource.Source.acceptFile(Path.of("1_test.pdf"), s));
    assertTrue(FileSystemSource.Source.acceptFile(Path.of("/test/1_test.pdf"), s));

    assertFalse(FileSystemSource.Source.acceptFile(Path.of("foobar.jpeg"), s));
    assertFalse(FileSystemSource.Source.acceptFile(Path.of("4_test.pdf"), s));
    assertFalse(FileSystemSource.Source.acceptFile(Path.of("no_1_test.pdf"), s));
    assertFalse(FileSystemSource.Source.acceptFile(Path.of("/test/no_1_test.pdf"), s));
  }

  @Test
  public void testSettings() {
    FileSystemSource.Settings s = new FileSystemSource.Settings();
    assertTrue(s.validate());

    s.setDelay(123);
    assertEquals(123L, s.getDelay());

    s.setRecursive(false);
    assertFalse(s.isRecursive());
    s.setRecursive(true);
    assertTrue(s.isRecursive());

    s.setWatching(false);
    assertFalse(s.isWatching());
    s.setWatching(true);
    assertTrue(s.isWatching());

    s.setReprocessOnModify(false);
    assertFalse(s.isReprocessOnModify());
    s.setReprocessOnModify(true);
    assertTrue(s.isReprocessOnModify());

    s.setNegateAcceptedFileNamePatterns(false);
    assertFalse(s.isNegateAcceptedFileNamePatterns());
    s.setNegateAcceptedFileNamePatterns(true);
    assertTrue(s.isNegateAcceptedFileNamePatterns());

    s.setRootFolder(Path.of("test"));
    assertEquals(Path.of("test"), s.getRootFolder());

    Set<Pattern> patterns = Set.of(Pattern.compile(".*\\.jpg"));
    s.setAcceptedFileNamePatterns(patterns);
    assertEquals(patterns, s.getAcceptedFileNamePatterns());
  }
}
