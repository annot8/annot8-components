/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class AnnotationsToTextFileTest {

  @Test
  public void testCaps() {
    AnnotationsToTextFile d = new AnnotationsToTextFile();
    assertNotNull(d.capabilities());
  }

  @Test
  public void testDeduplicate() throws IOException {
    Item item = createItem();

    Path path = Files.createTempFile("attf", ".txt");
    path.toFile().deleteOnExit();
    Files.write(path, List.of("A", "Z"));

    AnnotationsToTextFile.Settings s = new AnnotationsToTextFile.Settings();
    s.setOutputFile(path);
    s.setDeduplicate(true);
    s.setDeleteOnStart(false);
    s.setAnnotationTypes(List.of("grammar/letter", "entity/foo"));

    assertTrue(s.validate());

    try (AnnotationsToTextFile.Processor p = new AnnotationsToTextFile.Processor(s)) {
      p.process(item);

      List<String> output = Files.lines(path).collect(Collectors.toList());
      assertEquals(6, output.size());
      assertTrue(output.contains("A"));
      assertTrue(output.contains("B"));
      assertTrue(output.contains("C"));
      assertTrue(output.contains("D"));
      assertTrue(output.contains("E"));
      assertTrue(output.contains("Z"));
    }
  }

  @Test
  public void testDontDeduplicate() throws IOException {
    Item item = createItem();

    Path path = Files.createTempFile("attf", ".txt");
    path.toFile().deleteOnExit();
    Files.write(path, List.of("A", "Z"));

    AnnotationsToTextFile.Settings s = new AnnotationsToTextFile.Settings();
    s.setOutputFile(path);
    s.setDeduplicate(false);
    s.setDeleteOnStart(false);
    s.setAnnotationTypes(List.of("grammar/letter", "entity/foo"));

    assertTrue(s.validate());

    try (AnnotationsToTextFile.Processor p = new AnnotationsToTextFile.Processor(s)) {
      p.process(item);

      List<String> output = Files.lines(path).collect(Collectors.toList());
      assertEquals(8, output.size());
      assertEquals(2, output.stream().filter(x -> x.equals("A")).count());
      assertEquals(2, output.stream().filter(x -> x.equals("B")).count());
      assertTrue(output.contains("C"));
      assertTrue(output.contains("D"));
      assertTrue(output.contains("E"));
      assertTrue(output.contains("Z"));
    }
  }

  @Test
  public void testDeleteFile() throws IOException {
    Item item = createItem();

    Path path = Files.createTempFile("attf", ".txt");
    path.toFile().deleteOnExit();
    Files.write(path, List.of("A", "Z"));

    AnnotationsToTextFile.Settings s = new AnnotationsToTextFile.Settings();
    s.setOutputFile(path);
    s.setDeduplicate(true);
    s.setDeleteOnStart(true);
    s.setAnnotationTypes(List.of("grammar/letter", "entity/foo"));

    assertTrue(s.validate());

    try (AnnotationsToTextFile.Processor p = new AnnotationsToTextFile.Processor(s)) {
      p.process(item);

      List<String> output = Files.lines(path).collect(Collectors.toList());
      assertEquals(5, output.size());
      assertTrue(output.contains("A"));
      assertTrue(output.contains("B"));
      assertTrue(output.contains("C"));
      assertTrue(output.contains("D"));
      assertTrue(output.contains("E"));
    }
  }

  @Test
  public void testAllTypes() throws IOException {
    Item item = createItem();

    Path path = Files.createTempFile("attf", ".txt");
    path.toFile().deleteOnExit();
    Files.write(path, List.of("A", "Z"));

    AnnotationsToTextFile.Settings s = new AnnotationsToTextFile.Settings();
    s.setOutputFile(path);
    s.setDeduplicate(true);
    s.setDeleteOnStart(false);
    s.setAnnotationTypes(List.of());

    assertTrue(s.validate());

    try (AnnotationsToTextFile.Processor p = new AnnotationsToTextFile.Processor(s)) {
      p.process(item);

      List<String> output = Files.lines(path).collect(Collectors.toList());
      assertEquals(8, output.size());
      assertTrue(output.contains("A"));
      assertTrue(output.contains("B"));
      assertTrue(output.contains("C"));
      assertTrue(output.contains("D"));
      assertTrue(output.contains("E"));
      assertTrue(output.contains("Z"));
      assertTrue(output.contains("ABC"));
      assertTrue(output.contains("BCD"));
    }
  }

  private Item createItem() {
    Item item = new TestItem();
    Text t1 = item.createContent(Text.class).withData("ABC").save();

    Text t2 = item.createContent(Text.class).withData("BCD").save();

    t1.getAnnotations()
        .create()
        .withType("grammar/letter")
        .withBounds(new SpanBounds(0, 1))
        .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, "A")
        .save();

    t1.getAnnotations()
        .create()
        .withType("grammar/letter")
        .withBounds(new SpanBounds(1, 2))
        .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, "B")
        .save();

    t1.getAnnotations()
        .create()
        .withType("grammar/letter")
        .withBounds(new SpanBounds(2, 3))
        .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, "C")
        .save();

    t1.getAnnotations().create().withType("grammar/phrase").withBounds(new SpanBounds(0, 3)).save();

    t2.getAnnotations().create().withType("grammar/letter").withBounds(new SpanBounds(0, 1)).save();

    t2.getAnnotations()
        .create()
        .withType("grammar/letter")
        .withBounds(new SpanBounds(1, 2))
        .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, "E") // Note, different property to letter
        .save();

    t2.getAnnotations()
        .create()
        .withType("grammar/letter")
        .withBounds(new SpanBounds(2, 3))
        .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, "D")
        .save();

    t2.getAnnotations().create().withType("grammar/phrase").withBounds(new SpanBounds(0, 3)).save();

    return item;
  }
}
