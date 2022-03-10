/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

public class CsvFileTest {
  @Test
  public void test() {
    CsvFile.Settings settings = new CsvFile.Settings();

    Path path =
        new java.io.File(FileGazetteer.class.getResource("gazetteer.csv").getFile()).toPath();
    settings.setPath(path);
    settings.setType(AnnotationTypes.ANNOTATION_TYPE_PERSON);
    settings.setValueColumns(List.of("name"));
    settings.setAdditionalData(true);

    CsvFile f = new CsvFile();
    Processor p = f.createComponent(null, settings);

    Item item = new TestItem();

    Text content =
        item.createContent(TestStringContent.class).withData("Alice met Bob in the castle.").save();

    p.process(item);

    assertEquals(2, content.getAnnotations().getAll().count());

    List<String> annotations =
        content
            .getAnnotations()
            .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_PERSON)
            .sorted(SortUtils.SORT_BY_SPANBOUNDS)
            .map(
                a ->
                    content.getText(a).get()
                        + "_"
                        + a.getProperties().get("gender").orElse("?")
                        + a.getProperties().get("age").orElse("?"))
            .collect(Collectors.toList());

    assertEquals("Alice_F27", annotations.get(0));
    assertEquals("Bob_M30", annotations.get(1));
  }
}
