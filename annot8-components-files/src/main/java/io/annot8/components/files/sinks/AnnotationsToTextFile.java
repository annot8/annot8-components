/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import io.annot8.api.annotations.Annotation;
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
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@ComponentName("Annotations to Text File")
@ComponentDescription("Appends annotation values to a text file")
@SettingsClass(AnnotationsToTextFile.Settings.class)
public class AnnotationsToTextFile
    extends AbstractProcessorDescriptor<
        AnnotationsToTextFile.Processor, AnnotationsToTextFile.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesAnnotations("*", SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;
    private final Set<String> prev = new HashSet<>();

    public Processor(Settings settings) {
      this.settings = settings;

      if (settings.isDeleteOnStart()) {
        try {
          Files.deleteIfExists(settings.getOutputFile());
        } catch (IOException e) {
          log().warn("Unable to delete file {}", settings.getOutputFile(), e);
        }
      } else if (settings.isDeduplicate()) {
        try (Stream<String> lines = Files.lines(settings.getOutputFile())) {
          lines.map(String::strip).distinct().forEach(prev::add);
        } catch (FileNotFoundException fnfe) {
          // Do nothing - no existing file
        } catch (IOException e) {
          log()
              .warn(
                  "Could not read file {} to identify initial set of terms",
                  settings.getOutputFile(),
                  e);
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
      List<String> values = new ArrayList<>();
      item.getContents(Text.class)
          .forEach(
              t -> {
                Stream<Annotation> annotations = t.getAnnotations().getByBounds(SpanBounds.class);

                if (settings.getAnnotationTypes() != null
                    && !settings.getAnnotationTypes().isEmpty()) {
                  annotations =
                      annotations.filter(a -> settings.getAnnotationTypes().contains(a.getType()));
                }

                Stream<String> vals =
                    annotations
                        .map(
                            a ->
                                a.getProperties()
                                    .get(PropertyKeys.PROPERTY_KEY_VALUE, String.class)
                                    .orElse(
                                        a.getBounds(SpanBounds.class)
                                            .orElseThrow()
                                            .getData(t)
                                            .orElse(""))
                                    .strip())
                        .filter(s -> !s.isBlank());

                if (settings.isDeduplicate()) {
                  vals = vals.distinct().filter(prev::add);
                }

                vals.forEach(values::add);
              });

      if (!values.isEmpty()) {
        try {
          Files.write(settings.getOutputFile(), values, StandardOpenOption.APPEND);
        } catch (IOException e) {
          log().error("Unable to write annotation values to file {}", settings.getOutputFile(), e);
          return ProcessorResponse.processingError(e);
        }
      }

      return ProcessorResponse.ok();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private Path outputFile = Path.of("values.txt");
    private boolean deleteOnStart = false;
    private boolean deduplicate = true;
    private List<String> annotationTypes = List.of();

    @Override
    public boolean validate() {
      return outputFile != null && annotationTypes != null;
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

    @Description("If true, then only unique values will be added to the file")
    public boolean isDeduplicate() {
      return deduplicate;
    }

    public void setDeduplicate(boolean deduplicate) {
      this.deduplicate = deduplicate;
    }

    @Description("A list of annotation types to process, or leave empty to process all annotations")
    public List<String> getAnnotationTypes() {
      return annotationTypes;
    }

    public void setAnnotationTypes(List<String> annotationTypes) {
      this.annotationTypes = annotationTypes;
    }
  }
}
