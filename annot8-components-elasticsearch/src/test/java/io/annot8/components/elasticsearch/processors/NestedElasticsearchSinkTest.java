/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class NestedElasticsearchSinkTest {
  @Test
  public void testPersistData() {
    assertTrue(NestedElasticsearchSink.Processor.persistData(String.class));
    assertTrue(NestedElasticsearchSink.Processor.persistData(Integer.class));
    assertTrue(NestedElasticsearchSink.Processor.persistData(Long.class));
    assertTrue(NestedElasticsearchSink.Processor.persistData(Double.class));
    assertTrue(NestedElasticsearchSink.Processor.persistData(Boolean.class));

    assertFalse(NestedElasticsearchSink.Processor.persistData(InputStream.class));
    assertFalse(NestedElasticsearchSink.Processor.persistData(Map.class));
  }

  @Test
  public void testTransformData() {
    TestItem item = new TestItem();
    item.getProperties().set("author", "unknown");
    item.getProperties().set("rating", 4.5);

    TestStringContent text =
        item.createContent(TestStringContent.class)
            .withData("Jack and Jill went up the hill")
            .withProperty("line", 1)
            .withProperty("inEnglish", true)
            .save();

    Annotation aStanza =
        text.getAnnotations()
            .create()
            .withType("Stanza")
            .withBounds(ContentBounds.getInstance())
            .save();
    Annotation aJack =
        text.getAnnotations()
            .create()
            .withType("Person")
            .withBounds(new SpanBounds(0, 4))
            .withProperty("gender", "male")
            .save();
    Annotation aJill =
        text.getAnnotations().create().withType("Person").withBounds(new SpanBounds(9, 13)).save();

    Group g =
        item.getGroups()
            .create()
            .withType("group")
            .withAnnotation("member", aJack)
            .withAnnotation("member", aJill)
            .withProperty("groupSize", 2)
            .withProperty("groupName", "unknown")
            .save();

    Map<String, Object> m = NestedElasticsearchSink.Processor.transformItem(item);

    assertNull(m.get(NestedElasticsearchSink.Processor.PARENT));

    // Properties
    Map<String, Object> mProps =
        (Map<String, Object>) m.get(NestedElasticsearchSink.Processor.PROPERTIES);
    assertEquals(2, mProps.size());
    assertEquals("unknown", mProps.get("author"));
    assertEquals(4.5, mProps.get("rating"));

    // Contents
    List<Map<String, Object>> lContents =
        (List<Map<String, Object>>) m.get(NestedElasticsearchSink.Processor.CONTENTS);
    assertEquals(1, lContents.size());

    Map<String, Object> mContent = lContents.get(0);
    assertEquals(text.getId(), mContent.get(NestedElasticsearchSink.Processor.ID));
    assertEquals(
        text.getDescription(), mContent.get(NestedElasticsearchSink.Processor.DESCRIPTION));
    assertEquals(
        String.class.getName(), mContent.get(NestedElasticsearchSink.Processor.CONTENT_TYPE));
    assertEquals(
        "Jack and Jill went up the hill", mContent.get(NestedElasticsearchSink.Processor.CONTENT));

    Map<String, Object> mContentProps =
        (Map<String, Object>) mContent.get(NestedElasticsearchSink.Processor.PROPERTIES);
    assertEquals(2, mContentProps.size());
    assertEquals(true, mContentProps.get("inEnglish"));
    assertEquals(1, mContentProps.get("line"));

    // Annotations
    List<Map<String, Object>> lAnnotations =
        (List<Map<String, Object>>) mContent.get(NestedElasticsearchSink.Processor.ANNOTATIONS);
    assertEquals(3, lAnnotations.size());

    Map<String, Object> mStanza =
        lAnnotations.stream()
            .filter(map -> map.get(NestedElasticsearchSink.Processor.ID).equals(aStanza.getId()))
            .findFirst()
            .get();

    assertEquals(aStanza.getId(), mStanza.get(NestedElasticsearchSink.Processor.ID));
    assertNull(mStanza.get(NestedElasticsearchSink.Processor.BEGIN));
    assertNull(mStanza.get(NestedElasticsearchSink.Processor.END));
    assertNull(mStanza.get(NestedElasticsearchSink.Processor.VALUE));
    assertEquals("Stanza", mStanza.get(NestedElasticsearchSink.Processor.TYPE));
    assertEquals(
        ContentBounds.class.getName(), mStanza.get(NestedElasticsearchSink.Processor.BOUNDS_TYPE));
    assertNull(mStanza.get(NestedElasticsearchSink.Processor.PROPERTIES));

    Map<String, Object> mJack =
        lAnnotations.stream()
            .filter(map -> map.get(NestedElasticsearchSink.Processor.ID).equals(aJack.getId()))
            .findFirst()
            .get();

    assertEquals(aJack.getId(), mJack.get(NestedElasticsearchSink.Processor.ID));
    assertEquals(0, mJack.get(NestedElasticsearchSink.Processor.BEGIN));
    assertEquals(4, mJack.get(NestedElasticsearchSink.Processor.END));
    assertEquals("Jack", mJack.get(NestedElasticsearchSink.Processor.VALUE));
    assertEquals("Person", mJack.get(NestedElasticsearchSink.Processor.TYPE));
    assertEquals(
        SpanBounds.class.getName(), mJack.get(NestedElasticsearchSink.Processor.BOUNDS_TYPE));

    Map<String, Object> mJackProps =
        (Map<String, Object>) mJack.get(NestedElasticsearchSink.Processor.PROPERTIES);
    assertEquals(1, mJackProps.size());
    assertEquals("male", mJackProps.get("gender"));

    Map<String, Object> mJill =
        lAnnotations.stream()
            .filter(map -> map.get(NestedElasticsearchSink.Processor.ID).equals(aJill.getId()))
            .findFirst()
            .get();

    assertEquals(aJill.getId(), mJill.get(NestedElasticsearchSink.Processor.ID));
    assertEquals(9, mJill.get(NestedElasticsearchSink.Processor.BEGIN));
    assertEquals(13, mJill.get(NestedElasticsearchSink.Processor.END));
    assertEquals("Jill", mJill.get(NestedElasticsearchSink.Processor.VALUE));
    assertEquals("Person", mJill.get(NestedElasticsearchSink.Processor.TYPE));
    assertEquals(
        SpanBounds.class.getName(), mJill.get(NestedElasticsearchSink.Processor.BOUNDS_TYPE));
    assertNull(mJill.get(NestedElasticsearchSink.Processor.PROPERTIES));

    // Groups
    List<Map<String, Object>> lGroups =
        (List<Map<String, Object>>) m.get(NestedElasticsearchSink.Processor.GROUPS);
    assertEquals(1, lGroups.size());

    Map<String, Object> mGroup = lGroups.get(0);
    assertEquals(g.getId(), mGroup.get(NestedElasticsearchSink.Processor.ID));
    assertEquals("group", mGroup.get(NestedElasticsearchSink.Processor.TYPE));

    Map<String, Object> mGroupProps =
        (Map<String, Object>) mGroup.get(NestedElasticsearchSink.Processor.PROPERTIES);
    assertEquals(2, mGroupProps.size());
    assertEquals(2, mGroupProps.get("groupSize"));
    assertEquals("unknown", mGroupProps.get("groupName"));

    Map<String, Object> mGroupRoles =
        (Map<String, Object>) mGroup.get(NestedElasticsearchSink.Processor.ROLES);
    assertEquals(1, mGroupRoles.size());

    List<Map<String, Object>> lGroupMembers = (List<Map<String, Object>>) mGroupRoles.get("member");
    assertEquals(2, lGroupMembers.size());

    lGroupMembers.forEach(
        x -> assertEquals(text.getId(), x.get(NestedElasticsearchSink.Processor.CONTENT_ID)));

    assertEquals(
        1L,
        lGroupMembers.stream()
            .filter(
                x -> aJack.getId().equals(x.get(NestedElasticsearchSink.Processor.ANNOTATION_ID)))
            .count());
    assertEquals(
        1L,
        lGroupMembers.stream()
            .filter(
                x -> aJill.getId().equals(x.get(NestedElasticsearchSink.Processor.ANNOTATION_ID)))
            .count());
  }
}
