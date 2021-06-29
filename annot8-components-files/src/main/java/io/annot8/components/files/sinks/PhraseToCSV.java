/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import de.siegmar.fastcsv.writer.CsvWriter;
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
import io.annot8.common.data.utils.SortUtils;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@ComponentName("Phrases to CSV")
@ComponentDescription(
    "Appends phrase groups to a CSV in the format document,phraseType,value,start,end")
@SettingsClass(PhraseToCSV.Settings.class)
public class PhraseToCSV
    extends AbstractProcessorDescriptor<PhraseToCSV.Processor, PhraseToCSV.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesGroups(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
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

      if (settings.isDeleteOnStart()) {
        try {
          Files.deleteIfExists(settings.getOutputFile());
        } catch (IOException e) {
          log().warn("Unable to delete file {}", settings.getOutputFile(), e);
        }
      }

      try {
        Path parent = settings.getOutputFile().getParent();
        if (parent != null) Files.createDirectories(settings.getOutputFile().getParent());

        Files.createFile(settings.getOutputFile());
      } catch (FileAlreadyExistsException faee) {
        // Do nothing
      } catch (IOException ioe) {
        log().error("Unable to create output file {}", settings.getOutputFile(), ioe);
      }
    }

    @Override
    public ProcessorResponse process(Item item) {
      try (CsvWriter writer =
          CsvWriter.builder()
              .build(
                  settings.getOutputFile(),
                  Charset.defaultCharset(),
                  StandardOpenOption.CREATE,
                  StandardOpenOption.APPEND)) {
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
            .map(g -> groupToCsvRow(item, g))
            .filter(l -> !l.isEmpty())
            .forEach(writer::writeRow);

      } catch (IOException ioe) {
        log().error("Unable to write CSV file for {}", item.getId(), ioe);
        return ProcessorResponse.processingError(ioe);
      }

      return ProcessorResponse.ok();
    }

    protected List<String> groupToCsvRow(Item item, Group g) {
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
        return Collections.emptyList();
      }

      // Check all annotations are from the same content, otherwise it's not clear what the value is
      String contentId = annotations.get(0).getContentId();

      if (!annotations.stream().map(Annotation::getContentId).allMatch(a -> a.equals(contentId))) {
        log()
            .warn(
                "Group {} contains annotations from more than one Content, and will not be processed",
                g.getId());
        return Collections.emptyList();
      }

      // Check the content exists
      Optional<Content<?>> content = item.getContent(contentId);
      if (content.isEmpty()) {
        log().warn("Content {} not found", contentId);
        return Collections.emptyList();
      }

      // Get the beginning of the first annotation and the end of the last annotation
      int begin = annotations.get(0).getBounds(SpanBounds.class).get().getBegin();
      int end = annotations.get(annotations.size() - 1).getBounds(SpanBounds.class).get().getEnd();

      SpanBounds sb = new SpanBounds(begin, end);

      // Get the value that the phrase spans
      Optional<?> value = sb.getData(content.get());

      if (value.isEmpty()) {
        log().warn("Could not get data from Content {}", contentId);
        return Collections.emptyList();
      }

      // Write to CSV
      List<String> row = new ArrayList<>();
      row.add(item.getProperties().getOrDefault(PropertyKeys.PROPERTY_KEY_SOURCE, item.getId()));
      row.add(g.getProperties().getOrDefault(PropertyKeys.PROPERTY_KEY_SUBTYPE, ""));
      row.add(value.get().toString());
      row.add(String.valueOf(begin));
      row.add(String.valueOf(end));

      return row;
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Path outputFile = Path.of("phrases.csv");
    private boolean deleteOnStart = false;
    private List<String> phraseTypes = new ArrayList<>();

    @Override
    public boolean validate() {
      return outputFile != null && phraseTypes != null;
    }

    @Description("The file to output annotation values into")
    public Path getOutputFile() {
      return outputFile;
    }

    public void setOutputFile(Path outputFile) {
      this.outputFile = outputFile;
    }

    @Description("If true, the output file will be deleted when the pipeline first runs")
    public boolean isDeleteOnStart() {
      return deleteOnStart;
    }

    public void setDeleteOnStart(boolean deleteOnStart) {
      this.deleteOnStart = deleteOnStart;
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
