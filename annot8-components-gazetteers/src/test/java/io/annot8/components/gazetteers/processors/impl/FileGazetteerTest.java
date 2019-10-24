/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.annot8.components.gazetteers.processors.Gazetteer;

public class FileGazetteerTest {
  @Test
  public void test() {
    Path path = new File(this.getClass().getResource("gazetteer.txt").getFile()).toPath();

    Gazetteer g = new FileGazetteer(path, ',');

    assertEquals(7, g.getValues().size());
    assertTrue(g.getValues().contains("james"));
    assertTrue(g.getValues().contains("jimmy"));
    assertFalse(g.getValues().contains("elizabeth"));

    assertArrayEquals(
        Arrays.stream(new String[] {"james", "jim", "jimmy"}).sorted().toArray(),
        g.getAliases("jimmy").stream().sorted().toArray());

    assertEquals(Collections.emptyMap(), g.getAdditionalData("thom"));
  }
}
