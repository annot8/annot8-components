/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class TablesToCSVTest {
  @Test
  public void test() throws IOException {
    Item item = new TestItem();
    TableContent tc = item.createContent(TableContent.class).withData(new TestTable()).save();

    Path tempPath = Files.createTempDirectory("tablestocsv-test");

    TablesToCSV.Settings s = new TablesToCSV.Settings();
    s.setOutputFolder(tempPath);

    TablesToCSV.Processor p = new TablesToCSV.Processor(s);
    assertEquals(ProcessorResponse.ok(), p.process(item));

    Path csvFile = tempPath.resolve(item.getId()).resolve(tc.getId() + ".csv");
    assertTrue(csvFile.toFile().exists());

    List<String> lines = Files.lines(csvFile).collect(Collectors.toList());

    assertEquals("Name,Age,,Gender", lines.get(0));
    assertEquals("Alice,31,Red,F", lines.get(1));
    assertEquals("Bob,29", lines.get(2));

    Files.walk(tempPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
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
