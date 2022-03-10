/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.Annot8RuntimeException;
import io.annot8.api.settings.Description;
import io.annot8.components.gazetteers.processors.impl.MapGazetteer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ComponentName("CSV Gazetteer")
@ComponentDescription("Annotate terms within Text using a CSV file as the source")
@ComponentTags({"gazetteer", "file", "csv"})
@SettingsClass(CsvFile.Settings.class)
public class CsvFile extends AhoCorasick<CsvFile.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    Map<Set<String>, Map<String, Object>> termsAndData = new HashMap<>();
    try {
      NamedCsvReader reader = NamedCsvReader.builder().build(settings.getPath());
      reader.forEach(
          row -> {
            Set<String> terms = new HashSet<>();
            Map<String, Object> data = new HashMap<>();

            row.getFields()
                .forEach(
                    (key, val) -> {
                      if (settings.getValueColumns().contains(key)) {
                        terms.add(val);
                      } else {
                        data.put(key, val);
                      }
                    });
            termsAndData.put(terms, data);
          });
    } catch (IOException e) {
      throw new Annot8RuntimeException("Could not read CSV", e);
    }

    return new Processor(new MapGazetteer(termsAndData), settings);
  }

  public static class Settings extends AhoCorasick.Settings {
    private Path path;
    private List<String> valueColumns;

    @Description("The gazetteer CSV")
    public Path getPath() {
      return path;
    }

    public void setPath(Path path) {
      this.path = path;
    }

    @Description("The columns that contain the values to look for")
    public List<String> getValueColumns() {
      return valueColumns;
    }

    public void setValueColumns(List<String> valueColumns) {
      this.valueColumns = valueColumns;
    }

    @Override
    public boolean validate() {
      return super.validate() && path != null && valueColumns != null && !valueColumns.isEmpty();
    }
  }
}
