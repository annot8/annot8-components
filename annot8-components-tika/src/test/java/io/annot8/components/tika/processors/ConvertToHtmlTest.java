/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tika.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.testing.testimpl.TestItem;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

public class ConvertToHtmlTest {

  @Test
  public void test() {
    Processor extractor = new ConvertToHtml.Processor(true);

    Item item = new TestItem();
    item.createContent(InputStreamContent.class)
        .withData(ConvertToHtmlTest.class.getResourceAsStream("test.pdf"))
        .save();

    assertEquals(1, item.getContents(InputStreamContent.class).count());

    extractor.process(item);

    assertEquals(1, item.getContents(InputStreamContent.class).count());
    InputStreamContent isc = item.getContents(InputStreamContent.class).findFirst().get();
    String html =
        new BufferedReader(new InputStreamReader(isc.getData()))
            .lines()
            .collect(Collectors.joining("\n"));

    assertTrue(html.contains("<p>Hello world!</p>"));
  }
}
