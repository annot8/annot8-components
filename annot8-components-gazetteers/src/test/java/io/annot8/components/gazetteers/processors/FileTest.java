/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.components.gazetteers.processors.impl.FileGazetteer;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class FileTest {
  @Test
  public void test() {
    File.Settings settings = new File.Settings();

    Path path =
        new java.io.File(FileGazetteer.class.getResource("gazetteer.txt").getFile()).toPath();
    settings.setPath(path);
    settings.setType(AnnotationTypes.ANNOTATION_TYPE_PERSON);

    File f = new File();
    Processor p = f.createComponent(null, settings);

    Item item = new TestItem();

    Text content =
        item.createContent(TestStringContent.class)
            .withData("James went to visit Tom (also known as TOMMY), in London. Tommy is aged 32.")
            .save();

    p.process(item);

    assertEquals(1, item.getGroups().getAll().count());
    assertEquals(4, content.getAnnotations().getAll().count());

    List<String> annotations =
        content
            .getAnnotations()
            .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .sorted(SortUtils.SORT_BY_SPANBOUNDS)
            .map(a -> content.getText(a).get())
            .collect(Collectors.toList());

    assertEquals("James", annotations.get(0));
    assertEquals("Tom", annotations.get(1));
    assertEquals("TOMMY", annotations.get(2));
    assertEquals("Tommy", annotations.get(3));

    List<String> group =
        content
            .getItem()
            .getGroups()
            .getAll()
            .findFirst()
            .get()
            .getAnnotationsForContent(content)
            .map(a -> content.getText(a).get())
            .collect(Collectors.toList());

    assertTrue(group.contains("Tom"));
    assertTrue(group.contains("TOMMY"));
    assertTrue(group.contains("Tommy"));
  }

  @Test
  public void testCaseSensitive() {
    File.Settings settings = new File.Settings();

    Path path =
        new java.io.File(FileGazetteer.class.getResource("gazetteer.txt").getFile()).toPath();
    settings.setPath(path);
    settings.setType(AnnotationTypes.ANNOTATION_TYPE_PERSON);
    settings.setCaseSensitive(true);

    File f = new File();
    Processor p = f.createComponent(null, settings);

    Item item = new TestItem();

    Text content =
        item.createContent(TestStringContent.class)
            .withData("james went to visit tom (also known as TOMMY), in london.")
            .save();

    p.process(item);

    assertEquals(0, item.getGroups().getAll().count());
    assertEquals(2, content.getAnnotations().getAll().count());

    List<String> annotations =
        content
            .getAnnotations()
            .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .sorted(SortUtils.SORT_BY_SPANBOUNDS)
            .map(a -> content.getText(a).get())
            .collect(Collectors.toList());

    assertEquals("james", annotations.get(0));
    assertEquals("tom", annotations.get(1));
  }
}
