/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupRoles;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class PhraseToTableTest {

  @Test
  public void testSettings() {
    PhraseToTable.Settings s = new PhraseToTable.Settings();

    assertNotNull(s.getPhraseTypes());
    assertTrue(s.getPhraseTypes().isEmpty());
    s.setPhraseTypes(List.of("NP", "VP"));
    assertEquals(List.of("NP", "VP"), s.getPhraseTypes());

    assertTrue(s.validate());
  }

  @Test
  public void testDescriptor() {
    PhraseToTable desc = new PhraseToTable();
    assertNotNull(desc.capabilities());

    desc.setSettings(new PhraseToTable.Settings());

    PhraseToTable.Processor p = desc.create(new SimpleContext());
    assertNotNull(p);

    p.close();
  }

  @Test
  public void testGroupToTable() {
    Item item = createTestItem();
    Group g1 = item.getGroups().getById("group1").orElseThrow();
    Group g2 = item.getGroups().getById("group2").orElseThrow();
    Group g3 = item.getGroups().getById("group3").orElseThrow();

    Group emptyGroup = item.getGroups().getById("emptyGroup").orElseThrow();
    Group multipleContent = item.getGroups().getById("multipleContent").orElseThrow();

    PhraseToTable.Processor p = new PhraseToTable.Processor(new PhraseToTable.Settings());

    assertEquals(
        new PhraseToTable.PhraseRow(
            "foobar.txt", "textContent", "NP", "The bakery on the corner", 0, 24),
        p.groupToRow(item, g1).orElseThrow());
    assertEquals(
        new PhraseToTable.PhraseRow("foobar.txt", "textContent", "VP", "sells", 25, 30),
        p.groupToRow(item, g2).orElseThrow());
    assertEquals(
        new PhraseToTable.PhraseRow("foobar.txt", "textContent", "NP", "lots of pastries", 31, 47),
        p.groupToRow(item, g3).orElseThrow());

    assertEquals(Optional.empty(), p.groupToRow(item, emptyGroup));
    assertEquals(Optional.empty(), p.groupToRow(item, multipleContent));
  }

  @Test
  public void testProcess() {
    PhraseToTable.Settings s = new PhraseToTable.Settings();
    s.setPhraseTypes(List.of("NP"));

    Item item = createTestItem();

    try (PhraseToTable.Processor p = new PhraseToTable.Processor(s)) {
      p.process(item);

      assertEquals(1L, item.getContents(TableContent.class).count());
      TableContent tc = item.getContents(TableContent.class).findFirst().orElseThrow();

      assertNotNull(tc.getDescription());
      assertEquals(2, tc.getData().getRowCount());

      assertEquals("document", tc.getData().getColumnNames().orElseThrow().get(0));
      assertEquals("foobar.txt", tc.getData().getRow(0).orElseThrow().getValueAt(0).orElseThrow());
    }
  }

  private Item createTestItem() {
    Item item = new TestItem();
    item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, "foobar.txt");

    Text text =
        item.createContent(Text.class)
            .withData("The bakery on the corner sells lots of pastries.")
            .withId("textContent")
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
