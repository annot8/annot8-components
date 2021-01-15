/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

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
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ComponentName("Txt File Extractor")
@ComponentDescription("Extract text from a *.txt file and create Text content")
@SettingsClass(TxtFileExtractor.Settings.class)
public class TxtFileExtractor
    extends AbstractProcessorDescriptor<TxtFileExtractor.Processor, TxtFileExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(
        settings.isRemoveSourceContent(), settings.getExtensions(), settings.getCharset());
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(FileContent.class)
            .withCreatesContent(Text.class);

    if (getSettings().isRemoveSourceContent()) {
      builder = builder.withDeletesContent(FileContent.class);
    }

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    private final boolean removeSourceContent;
    private final List<String> extensions;
    private final Charset charset;

    public Processor(boolean removeSourceContent, List<String> extensions, String charset) {
      this.removeSourceContent = removeSourceContent;
      this.extensions = extensions;

      if (Charset.isSupported(charset)) {
        this.charset = Charset.forName(charset);
      } else {
        log()
            .error(
                "Charset {} is not supported - default charset {} will be used instead",
                charset,
                Charset.defaultCharset().name());
        this.charset = Charset.defaultCharset();
      }
    }

    @Override
    public ProcessorResponse process(Item item) {

      item.getContents(FileContent.class)
          .filter(
              f ->
                  extensions.isEmpty()
                      || extensions.contains(getExtension(f.getData().getName()).orElse("")))
          .forEach(
              f -> {
                try {
                  File file = f.getData();
                  String data = Files.readString(file.toPath(), charset);
                  item.createContent(Text.class)
                      .withDescription("Text from " + f.getId())
                      .withData(data)
                      .save();

                  // If processed, then remove it our item so it doesn't get reprocessed
                  if (removeSourceContent) item.removeContent(f);

                } catch (Exception e) {
                  log().warn("Unable to process file {}", f.getData().getAbsolutePath(), e);
                }
              });

      // Always carry on it
      return ProcessorResponse.ok();
    }

    private Optional<String> getExtension(String filename) {
      return Optional.ofNullable(filename)
          .filter(f -> f.contains("."))
          .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
    }
  }

  public static class Settings extends RemoveSourceContentSettings {
    private List<String> extensions = List.of("txt");
    private String charset = Charset.defaultCharset().name();

    @Description(
        "The list of file extensions on which this processor will act (case insensitive). If empty, then the processor will act on all files.")
    public List<String> getExtensions() {
      return extensions;
    }

    public void setExtensions(List<String> extensions) {
      this.extensions = extensions.stream().map(String::toLowerCase).collect(Collectors.toList());
    }

    @Description("The charset to read the files with")
    public String getCharset() {
      return charset;
    }

    public void setCharset(String charset) {
      this.charset = charset;
    }

    @Override
    public boolean validate() {
      return super.validate() && extensions != null && Charset.isSupported(charset);
    }
  }
}
