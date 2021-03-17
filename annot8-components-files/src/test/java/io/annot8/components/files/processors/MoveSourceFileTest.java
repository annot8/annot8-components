/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

public class MoveSourceFileTest {
  @Test
  public void testMove() throws IOException {
    Path input = Files.createTempDirectory("tmp-msf-input");
    Path output = Files.createTempDirectory("tmp-msf-output");

    File f = new File(input.toFile(), "example.txt");
    f.createNewFile();

    Item i = new TestItem();
    i.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, f);

    MoveSourceFile.Settings s = new MoveSourceFile.Settings();
    s.setUpdateSource(true);
    s.setFlatten(true);
    s.setCopyOriginalFile(false);
    s.setRootOutputFolder(output);

    MoveSourceFile.Processor p = new MoveSourceFile.Processor(s);
    ProcessorResponse pr = p.process(i);
    assertEquals(ProcessorResponse.ok(), pr);

    File fOut = new File(output.toFile(), "example.txt");
    assertTrue(fOut.exists());
    assertFalse(f.exists());

    assertEquals(
        fOut.toPath().toString(),
        i.getProperties().get(PropertyKeys.PROPERTY_KEY_SOURCE).get().toString());

    input.toFile().delete();
    fOut.delete();
    output.toFile().delete();
  }

  @Test
  public void testCopy() throws IOException {
    Path input = Files.createTempDirectory("tmp-msf-input");
    Path output = Files.createTempDirectory("tmp-msf-output");

    File f = new File(input.toFile(), "example.txt");
    f.createNewFile();

    Item i = new TestItem();
    i.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, f.toPath());

    MoveSourceFile.Settings s = new MoveSourceFile.Settings();
    s.setUpdateSource(false);
    s.setFlatten(true);
    s.setCopyOriginalFile(true);
    s.setRootOutputFolder(output);

    MoveSourceFile.Processor p = new MoveSourceFile.Processor(s);
    ProcessorResponse pr = p.process(i);
    assertEquals(ProcessorResponse.ok(), pr);

    File fOut = new File(output.toFile(), "example.txt");
    assertTrue(fOut.exists());
    assertTrue(f.exists());

    assertEquals(
        f.toPath().toString(),
        i.getProperties().get(PropertyKeys.PROPERTY_KEY_SOURCE).get().toString());

    f.delete();
    input.toFile().delete();
    fOut.delete();
    output.toFile().delete();
  }

  @Test
  public void testDirectory() throws IOException {
    Path input = Files.createTempDirectory("tmp-msf-input");
    Path output = Files.createTempDirectory("tmp-msf-output");

    File f = new File(input.toFile(), "example.txt");
    f.createNewFile();

    Item i = new TestItem();
    i.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, f.toString());

    MoveSourceFile.Settings s = new MoveSourceFile.Settings();
    s.setUpdateSource(true);
    s.setFlatten(false);
    s.setCopyOriginalFile(false);
    s.setRootOutputFolder(output);
    s.setBasePaths(List.of(input.getParent()));

    MoveSourceFile.Processor p = new MoveSourceFile.Processor(s);
    ProcessorResponse pr = p.process(i);
    assertEquals(ProcessorResponse.ok(), pr);

    File fOut = new File(output.resolve(input.getFileName()).toFile(), "example.txt");
    assertTrue(fOut.exists());
    assertFalse(f.exists());

    assertEquals(
        fOut.toPath().toString(),
        i.getProperties().get(PropertyKeys.PROPERTY_KEY_SOURCE).get().toString());

    input.toFile().delete();
    fOut.delete();
    output.resolve(input.getFileName()).toFile().delete();
    output.toFile().delete();
  }

  @Test
  public void testReplace() throws IOException {
    Path input = Files.createTempDirectory("tmp-msf-input");
    Path output = Files.createTempDirectory("tmp-msf-output");

    File f = new File(input.toFile(), "example.txt");
    f.createNewFile();

    Item i = new TestItem();
    i.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, f.toURI());

    MoveSourceFile.Settings s = new MoveSourceFile.Settings();
    s.setUpdateSource(true);
    s.setFlatten(true);
    s.setCopyOriginalFile(false);
    s.setRootOutputFolder(output);
    s.setReplaceExisting(true);

    File fOut = new File(output.toFile(), "example.txt");
    fOut.createNewFile();

    MoveSourceFile.Processor p = new MoveSourceFile.Processor(s);
    ProcessorResponse pr = p.process(i);
    assertEquals(ProcessorResponse.ok(), pr);

    assertTrue(fOut.exists());
    assertFalse(f.exists());

    assertEquals(
        fOut.toPath().toString(),
        i.getProperties().get(PropertyKeys.PROPERTY_KEY_SOURCE).get().toString());

    input.toFile().delete();
    fOut.delete();
    output.toFile().delete();
  }

  @Test
  public void testNoReplace() throws IOException {
    Path input = Files.createTempDirectory("tmp-msf-input");
    Path output = Files.createTempDirectory("tmp-msf-output");

    File f = new File(input.toFile(), "example.txt");
    f.createNewFile();

    Item i = new TestItem();
    i.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, f);

    MoveSourceFile.Settings s = new MoveSourceFile.Settings();
    s.setUpdateSource(true);
    s.setFlatten(true);
    s.setCopyOriginalFile(false);
    s.setRootOutputFolder(output);
    s.setReplaceExisting(false);

    File fOut = new File(output.toFile(), "example.txt");
    fOut.createNewFile();

    MoveSourceFile.Processor p = new MoveSourceFile.Processor(s);
    ProcessorResponse pr = p.process(i);
    assertEquals(ProcessorResponse.Status.ITEM_ERROR, pr.getStatus());

    f.delete();
    input.toFile().delete();
    output.toFile().delete();
  }

  @Test
  public void testNoSource() {
    Item i = new TestItem();

    MoveSourceFile.Settings s = new MoveSourceFile.Settings();
    MoveSourceFile.Processor p = new MoveSourceFile.Processor(s);

    ProcessorResponse pr = p.process(i);
    assertEquals(ProcessorResponse.ok(), pr);
  }

  @Test
  public void testBadSource() {
    Item i = new TestItem();
    i.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, 123);

    MoveSourceFile.Settings s = new MoveSourceFile.Settings();
    MoveSourceFile.Processor p = new MoveSourceFile.Processor(s);

    ProcessorResponse pr = p.process(i);
    assertEquals(ProcessorResponse.ok(), pr);
  }

  @Test
  public void testDescriptor() {
    MoveSourceFile msf = new MoveSourceFile();

    assertNotNull(msf.capabilities());

    MoveSourceFile.Settings s = new MoveSourceFile.Settings();
    assertTrue(s.validate());

    msf.setSettings(s);
    assertNotNull(msf.create(new SimpleContext()));
  }
}
