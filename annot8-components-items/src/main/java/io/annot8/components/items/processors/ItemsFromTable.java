/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ComponentName("Items from Table")
@ComponentDescription("Converts TableContent into separate items")
@SettingsClass(ItemsFromTable.Settings.class)
@ComponentTags({"item", "table"})
public class ItemsFromTable
    extends AbstractProcessorDescriptor<ItemsFromTable.Processor, ItemsFromTable.Settings> {

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder().withProcessesContent(TableContent.class);

    if (getSettings().getContent() != null && !getSettings().getContent().isEmpty()) {
      builder = builder.withCreatesContent(Text.class);
    }

    return builder.build();
  }

  @Override
  public Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getIgnore(), settings.getContent());
  }

  public static class Processor extends AbstractProcessor {

    private final List<String> ignore;
    private final List<String> content;

    public Processor(List<String> ignore, List<String> content) {
      if (ignore != null) {
        this.ignore = Collections.unmodifiableList(ignore);
      } else {
        this.ignore = Collections.emptyList();
      }

      if (content != null) {
        this.content = Collections.unmodifiableList(content);
      } else {
        this.content = Collections.emptyList();
      }
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents(TableContent.class)
          .forEach(
              tc -> {
                log().info("Extracting table {} ({})", tc.getId(), tc.getDescription());

                tc.getData()
                    .getRows()
                    .forEach(
                        row -> {
                          Item child = item.createChild();

                          child
                              .getProperties()
                              .set(
                                  PropertyKeys.PROPERTY_KEY_DESCRIPTION,
                                  "Row " + (row.getRowIndex() + 1) + " of Content " + tc.getId());
                          child
                              .getProperties()
                              .set(PropertyKeys.PROPERTY_KEY_INDEX, row.getRowIndex());

                          row.getColumnNames()
                              .forEach(
                                  column -> {
                                    if (ignore.contains(column) || column.isBlank()) return;

                                    if (content.contains(column)) {
                                      row.getString(column)
                                          .ifPresent(
                                              s ->
                                                  child
                                                      .createContent(Text.class)
                                                      .withData(s)
                                                      .withDescription(
                                                          "Column "
                                                              + column
                                                              + " of Row "
                                                              + (row.getRowIndex() + 1)
                                                              + " of Content "
                                                              + tc.getId())
                                                      .withPropertyIfPresent(
                                                          PropertyKeys.PROPERTY_KEY_INDEX,
                                                          row.getIndex(column))
                                                      .save());
                                    } else {
                                      row.getValueAt(column)
                                          .ifPresent(o -> child.getProperties().set(column, o));
                                    }
                                  });
                        });

                log()
                    .info(
                        "Finished extracting {} rows from table {}",
                        tc.getData().getRowCount(),
                        tc.getId());
              });

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> ignore = new ArrayList<>();
    private List<String> content = new ArrayList<>();

    @Override
    public boolean validate() {
      return true;
    }

    @Description(
        "A list of columns within the table that should be ignored. Columns with no column name are always ignored.")
    public List<String> getIgnore() {
      return ignore;
    }

    public void setIgnore(List<String> ignore) {
      this.ignore = ignore;
    }

    @Description(
        "A list of columns within the table that should be converted into Text content. Other columns will be converted into properties.")
    public List<String> getContent() {
      return content;
    }

    public void setContent(List<String> content) {
      this.content = content;
    }
  }
}
