/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.properties.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@ComponentName("Table to Properties")
@ComponentDescription(
    "Creates an Item level properties from 2-columned Table content. The first column is used as the property key, and the second as the value. Repeated keys will be merged into a list, or joint into a string.")
@SettingsClass(TableToProperties.Settings.class)
public class TableToProperties
    extends AbstractProcessorDescriptor<TableToProperties.Processor, TableToProperties.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesContent(TableContent.class).build();
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      Map<String, List<Row>> rows =
          item.getContents(TableContent.class)
              .filter(tc -> tc.getData().getColumnCount() == 2)
              .map(Content::getData)
              .flatMap(Table::getRows)
              .collect(
                  Collectors.groupingBy(
                      r -> {
                        String name = r.getString(0).orElse(null);

                        if (name == null || name.isBlank()) return null;

                        if (settings.getPropertyNameTransformation()
                            == StringTransformation.CAMEL_CASE) {
                          return toCamelCase(name);
                        } else {
                          return name;
                        }
                      }));

      rows.forEach(
          (k, r) -> {
            if (k.isBlank()) return;

            Object value;
            if (r.size() == 1) {
              value = r.get(0).getValueAt(1).orElse(null);
            } else {
              if (settings.getJoin() == null) {
                value =
                    r.stream()
                        .map(row -> row.getValueAt(1).orElse(null))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
              } else {
                value =
                    r.stream()
                        .map(row -> row.getValueAt(1).orElse(null))
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(settings.getJoin()));
              }
            }

            if (value == null) return;

            if (value instanceof String && ((String) value).isEmpty()) return;

            if (value instanceof List && ((List<?>) value).isEmpty()) return;

            item.getProperties().set(k, value);
          });

      if (settings.isDiscardTable()) {
        item.getContents(TableContent.class)
            .filter(tc -> tc.getData().getColumnCount() == 2)
            .forEach(tc -> item.removeContent(tc.getId()));
      }

      return ProcessorResponse.ok();
    }

    public static String toCamelCase(String name) {
      String lcase =
          name.replaceAll("/", " ").replaceAll("[^a-zA-Z0-9 ]", "").strip().toLowerCase();

      String[] s = lcase.split(" ");

      StringJoiner sj = new StringJoiner("");
      sj.add(s[0]);

      for (int i = 1; i < s.length; i++) {
        if (s[i].length() > 0) {
          sj.add(s[i].substring(0, 1).toUpperCase());
          sj.add(s[i].substring(1));
        }
      }

      return sj.toString();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private StringTransformation propertyNameTransformation = StringTransformation.CAMEL_CASE;
    private String join = null;
    private boolean discardTable = false;

    @Override
    public boolean validate() {
      return propertyNameTransformation != null;
    }

    @Description(
        "What transformation should be applied to property values prior to creation of the new property")
    public StringTransformation getPropertyNameTransformation() {
      return propertyNameTransformation;
    }

    public void setPropertyNameTransformation(StringTransformation propertyNameTransformation) {
      this.propertyNameTransformation = propertyNameTransformation;
    }

    @Description(
        "If null, then any values with the same key will be returned as a list. Otherwise, then values will be converted to strings and joined with this value.")
    public String getJoin() {
      return join;
    }

    public void setJoin(String join) {
      this.join = join;
    }

    @Description(
        "Should any processed tables be discarded after they have been converted into properties?")
    public boolean isDiscardTable() {
      return discardTable;
    }

    public void setDiscardTable(boolean discardTable) {
      this.discardTable = discardTable;
    }
  }

  public enum StringTransformation {
    NONE,
    CAMEL_CASE
  }
}
