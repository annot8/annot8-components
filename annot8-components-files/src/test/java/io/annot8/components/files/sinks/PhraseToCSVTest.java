/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupRoles;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

public class PhraseToCSVTest {

  @AfterAll
  static void deletePhrasesCsv() throws IOException {
    Files.deleteIfExists(Path.of("phrases.csv"));
  }

  @Test
  public void testSettings() {
    PhraseToCSV.Settings s = new PhraseToCSV.Settings();

    assertNotNull(s.getPhraseTypes());
    assertTrue(s.getPhraseTypes().isEmpty());
    s.setPhraseTypes(List.of("NP", "VP"));
    assertEquals(List.of("NP", "VP"), s.getPhraseTypes());

    s.setDeleteOnStart(true);
    assertTrue(s.isDeleteOnStart());
    s.setDeleteOnStart(false);
    assertFalse(s.isDeleteOnStart());

    assertNotNull(s.getOutputFile());
    s.setOutputFile(Path.of("test.txt"));
    assertEquals(Path.of("test.txt"), s.getOutputFile());

    assertTrue(s.validate());
  }

  @Test
  public void testDescriptor() {
    PhraseToCSV desc = new PhraseToCSV();
    assertNotNull(desc.capabilities());

    desc.setSettings(new PhraseToCSV.Settings());

    PhraseToCSV.Processor p = desc.create(new SimpleContext());
    assertNotNull(p);

    p.close();
  }

  @Test
  public void testGroupToCsv() {
    Item item = createTestItem();
    Group g1 = item.getGroups().getById("group1").orElseThrow();
    Group g2 = item.getGroups().getById("group2").orElseThrow();
    Group g3 = item.getGroups().getById("group3").orElseThrow();

    Group emptyGroup = item.getGroups().getById("emptyGroup").orElseThrow();
    Group multipleContent = item.getGroups().getById("multipleContent").orElseThrow();

    PhraseToCSV.Processor p = new PhraseToCSV.Processor(new PhraseToCSV.Settings());

    assertEquals(
        List.of("foobar.txt", "NP", "The bakery on the corner", "0", "24"),
        p.groupToCsvRow(item, g1));
    assertEquals(List.of("foobar.txt", "VP", "sells", "25", "30"), p.groupToCsvRow(item, g2));
    assertEquals(
        List.of("foobar.txt", "NP", "lots of pastries", "31", "47"), p.groupToCsvRow(item, g3));

    assertEquals(Collections.emptyList(), p.groupToCsvRow(item, emptyGroup));
    assertEquals(Collections.emptyList(), p.groupToCsvRow(item, multipleContent));
  }

  @Test
  public void testProcess() throws IOException {
    Path tempFile = Files.createTempFile("test", ".csv");
    tempFile.toFile().deleteOnExit();

    PhraseToCSV.Settings s = new PhraseToCSV.Settings();
    s.setOutputFile(tempFile);
    s.setDeleteOnStart(true);
    s.setPhraseTypes(List.of("NP"));

    PhraseToCSV.Processor p = new PhraseToCSV.Processor(s);
    p.process(createTestItem());

    List<String> output = Files.readAllLines(tempFile);

    assertEquals(2, output.size());
    assertTrue(output.contains("foobar.txt,NP,The bakery on the corner,0,24"));
    assertTrue(output.contains("foobar.txt,NP,lots of pastries,31,47"));
  }

  private Item createTestItem() {
    Item item = new TestItem();
    item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, "foobar.txt");

    Text text =
        item.createContent(Text.class)
            .withData("The bakery on the corner sells lots of pastries.")
            .save();

    text.getAnnotations()
        .create()
        .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
        .withBounds(new SpanBounds(0, 48))
        .save();

    Annotation a1 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(0, 3))
            .save();
    Annotation a2 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(4, 10))
            .save();
    Annotation a3 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(11, 13))
            .save();
    Annotation a4 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(14, 17))
            .save();
    Annotation a5 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(18, 24))
            .save();
    Annotation a6 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(25, 30))
            .save();
    Annotation a7 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(31, 35))
            .save();
    Annotation a8 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(36, 38))
            .save();
    Annotation a9 =
        text.getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
            .withBounds(new SpanBounds(39, 47))
            .save();

    // Valid groups
    item.getGroups()
        .create()
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a1)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_HEAD, a2)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a3)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a4)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a5)
        .withType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .withProperty(PropertyKeys.PROPERTY_KEY_SUBTYPE, "NP")
        .withId("group1")
        .save();

    item.getGroups()
        .create()
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_HEAD, a6)
        .withType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .withProperty(PropertyKeys.PROPERTY_KEY_SUBTYPE, "VP")
        .withId("group2")
        .save();

    item.getGroups()
        .create()
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a7)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, a8)
        .withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_HEAD, a9)
        .withType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .withProperty(PropertyKeys.PROPERTY_KEY_SUBTYPE, "NP")
        .withId("group3")
        .save();

    // Empty group
    item.getGroups()
        .create()
        .withId("emptyGroup")
        .withType(GroupTypes.GROUP_PREFIX + "empty")
        .save();

    // Multiple content
    Text text2 = item.createContent(Text.class).withData("The bakery sells bread").save();

    Annotation bakery =
        text2
            .getAnnotations()
            .create()
            .withType(AnnotationTypes.ANNOTATION_TYPE_LOCATION)
            .withBounds(new SpanBounds(0, 10))
            .save();

    item.getGroups()
        .create()
        .withAnnotation(GroupRoles.GROUP_ROLE_MENTION, a2)
        .withAnnotation(GroupRoles.GROUP_ROLE_MENTION, bakery)
        .withType(GroupTypes.GROUP_TYPE_SAMEAS)
        .withId("multipleContent")
        .save();

    return item;
  }
}
