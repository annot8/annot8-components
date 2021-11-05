/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import de.siegmar.fastcsv.writer.CsvWriter;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@ComponentName("Table Content to CSV")
@ComponentDescription("Saves Table Content as a CSV file")
@SettingsClass(TablesToCSV.Settings.class)
public class TablesToCSV
    extends AbstractProcessorDescriptor<TablesToCSV.Processor, TablesToCSV.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesContent(TableContent.class).build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Exception> exceptions = new ArrayList<>();

      item.getContents(TableContent.class)
          .forEach(
              t -> {
                Path p =
                    settings.getOutputFolder().resolve(item.getId()).resolve(t.getId() + ".csv");
                try {
                  Files.createDirectories(p.getParent());
                } catch (IOException e) {
                  log().error("Could not create directory {}", p.getParent());
                  exceptions.add(e);
                }

                try (CsvWriter writer = CsvWriter.builder().build(p, Charset.defaultCharset())) {
                  Table tbl = t.getData();
                  if (tbl.getColumnNames().isPresent()) writer.writeRow(tbl.getColumnNames().get());

                  tbl.getRows()
                      .map(
                          r -> {
                            List<String> s = new ArrayList<>(r.getColumnCount());

                            for (int i = 0; i < r.getColumnCount(); i++) {
                              s.add(r.getValueAt(i).orElse("").toString());
                            }

                            return s;
                          })
                      .filter(l -> !l.stream().allMatch(String::isBlank))
                      .forEach(writer::writeRow);

                } catch (IOException ioe) {
                  log().error("Unable to write CSV file for {}", t.getId(), ioe);
                  exceptions.add(ioe);
                }
              });

      if (exceptions.isEmpty()) {
        return ProcessorResponse.ok();
      } else {
        return ProcessorResponse.itemError(exceptions);
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Path outputFolder = Path.of("./csv");

    @Override
    public boolean validate() {
      return outputFolder != null;
    }

    @Description("The folder to save CSV files in")
    public Path getOutputFolder() {
      return outputFolder;
    }

    public void setOutputFolder(Path outputFolder) {
      this.outputFolder = outputFolder;
    }
  }
}
