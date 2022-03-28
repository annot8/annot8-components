/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.text.processors;

import static org.junit.jupiter.api.Assertions.fail;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.components.Processor;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8Exception;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegexProcessorTest {

  @Test
  public void testRegexFromConstructor() {
    try (Processor p = new RegexProcessor(Pattern.compile("[0-9]+"), 0, "number")) {
      assertProcessorCorrectness(p);
    } catch (Annot8Exception e) {
      fail("Error not expected in this test", e);
    }
  }

  @Test
  public void testRegexFromSettings() {
    RegexSettings rs = new RegexSettings(Pattern.compile("[0-9]+"), 0, "number");
    try (Processor p = new RegexProcessor(rs)) {
      assertProcessorCorrectness(p);
    } catch (Annot8Exception e) {
      fail("Error not expected in this test", e);
    }
  }

  private void assertProcessorCorrectness(Processor processor) throws Annot8Exception {
    Item item = new TestItem();
    Text content =
        item.createContent(TestStringContent.class)
            .withDescription("test")
            .withData("x + 12 = 42")
            .save();

    processor.process(item);

    AnnotationStore store = content.getAnnotations();

    List<Annotation> annotations = store.getAll().collect(Collectors.toList());
    Assertions.assertEquals(2, annotations.size());

    for (Annotation annotation : annotations) {
      Assertions.assertEquals("number", annotation.getType());
      Assertions.assertEquals(content.getId(), annotation.getContentId());
      SpanBounds bounds = annotation.getBounds(SpanBounds.class).get();
      String value = bounds.getData(content).get();
      // Basic impl to handle order not being guaranteed
      switch (value) {
        case "42":
          Assertions.assertEquals(9, bounds.getBegin());
          Assertions.assertEquals(11, bounds.getEnd());
          break;
        case "12":
          Assertions.assertEquals(4, bounds.getBegin());
          Assertions.assertEquals(6, bounds.getEnd());
          break;
        default:
          fail("Unexpected value " + value + " detected");
          break;
      }
    }
  }
}
