/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

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
import io.annot8.common.data.content.Text;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@ComponentName("Text to File")
@ComponentDescription("Save Text Content to a file on disk")
@SettingsClass(TextToFile.Settings.class)
public class TextToFile
    extends AbstractProcessorDescriptor<TextToFile.Processor, TextToFile.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesContent(Text.class).build();
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

      item.getContents(Text.class)
          .forEach(
              t -> {
                Path p =
                    settings.getOutputFolder().resolve(item.getId()).resolve(t.getId() + ".txt");
                try {
                  Files.createDirectories(p.getParent());
                } catch (IOException e) {
                  log().error("Could not create directory {}", p.getParent());
                  exceptions.add(e);
                }

                try {
                  Files.writeString(p, t.getData());
                } catch (IOException ioe) {
                  log().error("Unable to write text file for {}", t.getId(), ioe);
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
    private Path outputFolder = Path.of("./text");

    @Override
    public boolean validate() {
      return outputFolder != null;
    }

    @Description("The folder to save text files into")
    public Path getOutputFolder() {
      return outputFolder;
    }

    public void setOutputFolder(Path outputFolder) {
      this.outputFolder = outputFolder;
    }
  }
}
