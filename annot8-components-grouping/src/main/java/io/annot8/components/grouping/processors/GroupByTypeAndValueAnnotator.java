/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.grouping.processors;

import java.util.Optional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.core.annotations.Annotation;
import io.annot8.core.annotations.Group;
import io.annot8.core.capabilities.CreatesGroup;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.IncompleteException;
import io.annot8.core.stores.GroupStore;

@CreatesGroup(GroupByTypeAndValueAnnotator.TYPE)
public class GroupByTypeAndValueAnnotator extends AbstractTextProcessor {

  public static final String TYPE = "exactMatches";
  private static final String ROLE = "as";

  @Override
  protected void process(Item item, Text content) {

    SetMultimap<String, Annotation> map = HashMultimap.create();

    // Collate up all the annotations which have the same
    // TODO: Doing this over all bounds (but it could just be done over spanbounds)

    content
        .getAnnotations()
        .getAll()
        .forEach(
            a -> {
              Optional<String> optional = content.getText(a);
              optional.ifPresent(
                  covered -> {
                    String key = toKey(a.getType(), covered);
                    map.put(key, a);
                  });
            });

    // Create a group for things which are the same
    GroupStore groupStore = item.getGroups();
    map.asMap()
        .values()
        .forEach(
            annotations -> {
              Group.Builder builder = groupStore.create().withType(TYPE);

              annotations.forEach(a -> builder.withAnnotation(ROLE, a));

              try {
                builder.save();
              } catch (IncompleteException e) {
                log().info("Unable to save group", e);
              }
            });
  }

  private String toKey(String type, String covered) {
    return type + ":" + covered;
  }
}
