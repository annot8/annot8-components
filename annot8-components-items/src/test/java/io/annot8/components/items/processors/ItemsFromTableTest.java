/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.TestItemFactory;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class ItemsFromTableTest {
  @Test
  public void test() {
    TestItem item = new TestItem();
    TestItemFactory itemFactory = (TestItemFactory) item.getItemFactory();

    item.createContent(TableContent.class)
        .withData(new TestTable())
        .withDescription("Test Table")
        .save();

    Processor p = new ItemsFromTable.Processor(List.of("Gender"), List.of("Name"));
    ProcessorResponse pr = p.process(item);
    assertEquals(ProcessorResponse.ok(), pr);

    List<Item> children = itemFactory.getCreatedItems();
    assertEquals(2, children.size());

    // Requires Annot8 1.0.1 due to bug in testing
    // children.forEach(i -> assertEquals(item.getId(), i.getParent().get()));

    Item child1 = children.get(0);
    assertEquals(31, child1.getProperties().get("Age").get());

    Text text1 = child1.getContents(Text.class).findFirst().get();
    assertEquals("Alice", text1.getData());

    Item child2 = children.get(1);
    assertEquals(29, child2.getProperties().get("Age").get());

    Text text2 = child2.getContents(Text.class).findFirst().get();
    assertEquals("Bob", text2.getData());
  }

  public static class TestTable implements Table {

    @Override
    public int getColumnCount() {
      return 4;
    }

    @Override
    public int getRowCount() {
      return 2;
    }

    @Override
    public Optional<List<String>> getColumnNames() {
      return Optional.of(List.of("Name", "Age", "", "Gender"));
    }

    @Override
    public Stream<Row> getRows() {
      return Stream.of(
          new Row() {
            @Override
            public List<String> getColumnNames() {
              return List.of("Name", "Age", "", "Gender");
            }

            @Override
            public int getColumnCount() {
              return 4;
            }

            @Override
            public int getRowIndex() {
              return 0;
            }

            @Override
            public Optional<Object> getValueAt(int index) {
              switch (index) {
                case 0:
                  return Optional.of("Alice");
                case 1:
                  return Optional.of(31);
                case 2:
                  return Optional.of("Red");
                case 3:
                  return Optional.of("F");
                default:
                  return Optional.empty();
              }
            }
          },
          new Row() {
            @Override
            public List<String> getColumnNames() {
              return List.of("Name", "Age");
            }

            @Override
            public int getColumnCount() {
              return 2;
            }

            @Override
            public int getRowIndex() {
              return 1;
            }

            @Override
            public Optional<Object> getValueAt(int index) {
              switch (index) {
                case 0:
                  return Optional.of("Bob");
                case 1:
                  return Optional.of(29);
                default:
                  return Optional.empty();
              }
            }
          });
    }
  }
}
