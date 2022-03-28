/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.components.elasticsearch.ElasticsearchUtils;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
class NestedElasticsearchSinkTest {

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

    Map<String, Object> m = NestedElasticsearchSink.Processor.transformItem(item, false);

    assertNull(m.get(ElasticsearchUtils.PARENT));

    // Properties
    Map<String, Object> mProps = (Map<String, Object>) m.get(ElasticsearchUtils.PROPERTIES);
    assertEquals(2, mProps.size());
    assertEquals("unknown", mProps.get("author"));
    assertEquals(4.5, mProps.get("rating"));

    // Contents
    List<Map<String, Object>> lContents =
        (List<Map<String, Object>>) m.get(NestedElasticsearchSink.Processor.CONTENTS);
    assertEquals(1, lContents.size());

    Map<String, Object> mContent = lContents.get(0);
    assertEquals(text.getId(), mContent.get(ElasticsearchUtils.ID));
    assertEquals(text.getDescription(), mContent.get(ElasticsearchUtils.DESCRIPTION));
    assertEquals(String.class.getName(), mContent.get(ElasticsearchUtils.CONTENT_TYPE));
    assertEquals("Jack and Jill went up the hill", mContent.get(ElasticsearchUtils.CONTENT));

    Map<String, Object> mContentProps =
        (Map<String, Object>) mContent.get(ElasticsearchUtils.PROPERTIES);
    assertEquals(2, mContentProps.size());
    assertEquals(true, mContentProps.get("inEnglish"));
    assertEquals(1, mContentProps.get("line"));

    // Annotations
    List<Map<String, Object>> lAnnotations =
        (List<Map<String, Object>>) mContent.get(NestedElasticsearchSink.Processor.ANNOTATIONS);
    assertEquals(3, lAnnotations.size());

    Map<String, Object> mStanza =
        lAnnotations.stream()
            .filter(map -> map.get(ElasticsearchUtils.ID).equals(aStanza.getId()))
            .findFirst()
            .get();

    assertEquals(aStanza.getId(), mStanza.get(ElasticsearchUtils.ID));
    assertNull(mStanza.get(ElasticsearchUtils.BEGIN));
    assertNull(mStanza.get(ElasticsearchUtils.END));
    assertNull(mStanza.get(ElasticsearchUtils.VALUE));
    assertEquals("Stanza", mStanza.get(ElasticsearchUtils.TYPE));
    assertEquals(ContentBounds.class.getName(), mStanza.get(ElasticsearchUtils.BOUNDS_TYPE));
    assertNull(mStanza.get(ElasticsearchUtils.PROPERTIES));

    Map<String, Object> mJack =
        lAnnotations.stream()
            .filter(map -> map.get(ElasticsearchUtils.ID).equals(aJack.getId()))
            .findFirst()
            .get();

    assertEquals(aJack.getId(), mJack.get(ElasticsearchUtils.ID));
    assertEquals(0, mJack.get(ElasticsearchUtils.BEGIN));
    assertEquals(4, mJack.get(ElasticsearchUtils.END));
    assertEquals("Jack", mJack.get(ElasticsearchUtils.VALUE));
    assertEquals("Person", mJack.get(ElasticsearchUtils.TYPE));
    assertEquals(SpanBounds.class.getName(), mJack.get(ElasticsearchUtils.BOUNDS_TYPE));

    Map<String, Object> mJackProps = (Map<String, Object>) mJack.get(ElasticsearchUtils.PROPERTIES);
    assertEquals(1, mJackProps.size());
    assertEquals("male", mJackProps.get("gender"));

    Map<String, Object> mJill =
        lAnnotations.stream()
            .filter(map -> map.get(ElasticsearchUtils.ID).equals(aJill.getId()))
            .findFirst()
            .get();

    assertEquals(aJill.getId(), mJill.get(ElasticsearchUtils.ID));
    assertEquals(9, mJill.get(ElasticsearchUtils.BEGIN));
    assertEquals(13, mJill.get(ElasticsearchUtils.END));
    assertEquals("Jill", mJill.get(ElasticsearchUtils.VALUE));
    assertEquals("Person", mJill.get(ElasticsearchUtils.TYPE));
    assertEquals(SpanBounds.class.getName(), mJill.get(ElasticsearchUtils.BOUNDS_TYPE));
    assertNull(mJill.get(ElasticsearchUtils.PROPERTIES));

    // Groups
    List<Map<String, Object>> lGroups =
        (List<Map<String, Object>>) m.get(NestedElasticsearchSink.Processor.GROUPS);
    assertEquals(1, lGroups.size());

    Map<String, Object> mGroup = lGroups.get(0);
    assertEquals(g.getId(), mGroup.get(ElasticsearchUtils.ID));
    assertEquals("group", mGroup.get(ElasticsearchUtils.TYPE));

    Map<String, Object> mGroupProps =
        (Map<String, Object>) mGroup.get(ElasticsearchUtils.PROPERTIES);
    assertEquals(2, mGroupProps.size());
    assertEquals(2, mGroupProps.get("groupSize"));
    assertEquals("unknown", mGroupProps.get("groupName"));

    Map<String, Object> mGroupRoles = (Map<String, Object>) mGroup.get(ElasticsearchUtils.ROLES);
    assertEquals(1, mGroupRoles.size());

    List<Map<String, Object>> lGroupMembers = (List<Map<String, Object>>) mGroupRoles.get("member");
    assertEquals(2, lGroupMembers.size());

    lGroupMembers.forEach(x -> assertEquals(text.getId(), x.get(ElasticsearchUtils.CONTENT_ID)));

    assertEquals(
        1L,
        lGroupMembers.stream()
            .filter(x -> aJack.getId().equals(x.get(ElasticsearchUtils.ANNOTATION_ID)))
            .count());
    assertEquals(
        1L,
        lGroupMembers.stream()
            .filter(x -> aJill.getId().equals(x.get(ElasticsearchUtils.ANNOTATION_ID)))
            .count());
  }
}
