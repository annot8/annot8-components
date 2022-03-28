/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sinks;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

  public static class Processor extends AbstractTextProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    protected void process(Text content) {
      Path p =
          settings
              .getOutputFolder()
              .resolve(content.getItem().getId())
              .resolve(content.getId() + ".txt");
      try {
        Files.createDirectories(p.getParent());
      } catch (IOException e) {
        throw new ProcessingException("Could not create directory", e);
      }

      try {
        Files.writeString(p, content.getData());
      } catch (IOException e) {
        throw new ProcessingException("Unable to write text file", e);
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
