/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.text.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
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
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Row;
import io.annot8.common.data.content.Table;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentName("Phrases to Table")
@ComponentDescription(
    "Converts phrase groups to a table in the format document,content,phraseType,value,start,end")
@SettingsClass(PhraseToTable.Settings.class)
public class PhraseToTable
    extends AbstractProcessorDescriptor<PhraseToTable.Processor, PhraseToTable.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesGroups(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .withCreatesContent(TableContent.class)
        .build();
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
      List<PhraseRow> rows =
          item.getGroups()
              .getByType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
              .filter(
                  g ->
                      settings.getPhraseTypes().isEmpty()
                          || settings
                              .getPhraseTypes()
                              .contains(
                                  g.getProperties()
                                      .getOrDefault(PropertyKeys.PROPERTY_KEY_SUBTYPE, "")))
              .map(g -> groupToRow(item, g))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toList());

      for (int i = 0; i < rows.size(); i++) rows.get(i).setRowIndex(i);

      Table table =
          new Table() {
            @Override
            public int getColumnCount() {
              return PhraseRow.COLUMN_HEADINGS.size();
            }

            @Override
            public int getRowCount() {
              return rows.size();
            }

            @Override
            public Optional<List<String>> getColumnNames() {
              return Optional.of(PhraseRow.COLUMN_HEADINGS);
            }

            @Override
            public Stream<Row> getRows() {
              return rows.stream().map(r -> r);
            }
          };

      item.createContent(TableContent.class)
          .withData(table)
          .withDescription("Phrases from " + item.getId())
          .save();

      return ProcessorResponse.ok();
    }

    protected Optional<PhraseRow> groupToRow(Item item, Group g) {
      // Get all annotations with SpanBounds and sort
      List<Annotation> annotations =
          g.getAnnotations().values().stream()
              .flatMap(Function.identity())
              .filter(a -> a.getBounds(SpanBounds.class).isPresent())
              .sorted(SortUtils.SORT_BY_SPANBOUNDS)
              .collect(Collectors.toList());

      // Check we have some annotations
      if (annotations.isEmpty()) {
        log().info("Group {} is empty of suitable annotations", g.getId());
        return Optional.empty();
      }

      // Check all annotations are from the same content, otherwise it's not clear what the value is
      String contentId = annotations.get(0).getContentId();

      if (!annotations.stream().map(Annotation::getContentId).allMatch(a -> a.equals(contentId))) {
        log()
            .warn(
                "Group {} contains annotations from more than one Content, and will not be processed",
                g.getId());
        return Optional.empty();
      }

      // Check the content exists
      Optional<Content<?>> content = item.getContent(contentId);
      if (content.isEmpty()) {
        log().warn("Content {} not found", contentId);
        return Optional.empty();
      }

      // Get the beginning of the first annotation and the end of the last annotation
      int begin = annotations.get(0).getBounds(SpanBounds.class).get().getBegin();
      int end = annotations.get(annotations.size() - 1).getBounds(SpanBounds.class).get().getEnd();

      SpanBounds sb = new SpanBounds(begin, end);

      // Get the value that the phrase spans
      Optional<?> value = sb.getData(content.get());

      if (value.isEmpty()) {
        log().warn("Could not get data from Content {}", contentId);
        return Optional.empty();
      }

      return Optional.of(
          new PhraseRow(
              item.getProperties().getOrDefault(PropertyKeys.PROPERTY_KEY_SOURCE, item.getId()),
              contentId,
              g.getProperties().getOrDefault(PropertyKeys.PROPERTY_KEY_SUBTYPE, ""),
              value.get(),
              begin,
              end));
    }
  }

  public static class PhraseRow implements Row {
    public static final List<String> COLUMN_HEADINGS =
        List.of("document", "content", "phraseType", "value", "start", "end");

    private int rowIndex = 0;
    private final List<Object> values;

    public PhraseRow(
        String documentId, String content, String phraseType, Object value, int start, int end) {
      this.values = List.of(documentId, content, phraseType, value, start, end);
    }

    @Override
    public List<String> getColumnNames() {
      return COLUMN_HEADINGS;
    }

    @Override
    public int getColumnCount() {
      return COLUMN_HEADINGS.size();
    }

    public void setRowIndex(int rowIndex) {
      this.rowIndex = rowIndex;
    }

    @Override
    public int getRowIndex() {
      return rowIndex;
    }

    @Override
    public Optional<Object> getValueAt(int index) {
      if (index < 0 || index >= values.size()) {
        return Optional.empty();
      } else {
        return Optional.of(values.get(index));
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      PhraseRow phraseRow = (PhraseRow) o;
      return rowIndex == phraseRow.rowIndex && Objects.equals(values, phraseRow.values);
    }

    @Override
    public int hashCode() {
      return Objects.hash(rowIndex, values);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> phraseTypes = new ArrayList<>();

    @Override
    public boolean validate() {
      return phraseTypes != null;
    }

    @Description(
        "A list of the phrase types (e.g. NP) that should be persisted. If empty, all phrase types are persisted.")
    public List<String> getPhraseTypes() {
      return phraseTypes;
    }

    public void setPhraseTypes(List<String> phraseTypes) {
      this.phraseTypes = phraseTypes;
    }
  }
}
