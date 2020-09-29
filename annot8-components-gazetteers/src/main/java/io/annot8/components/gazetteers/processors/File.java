/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
import io.annot8.components.gazetteers.processors.impl.FileGazetteer;
import java.nio.file.Path;

@ComponentName("File Gazetteer")
@ComponentDescription("Annotate terms within Text using an external file as the gazetteer")
@ComponentTags({"gazetteer", "file"})
@SettingsClass(File.Settings.class)
public class File extends AhoCorasick<File.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(new FileGazetteer(settings.getPath(), settings.getSeparator()), settings);
  }

  public static class Settings extends AhoCorasick.Settings {
    private Path path;
    private char separator = ',';

    @Description("The gazetteer file")
    public Path getPath() {
      return path;
    }

    public void setPath(Path path) {
      this.path = path;
    }

    @Description("The separator used to separate aliases on a single line of the gazetteer")
    public char getSeparator() {
      return separator;
    }

    public void setSeparator(char separator) {
      this.separator = separator;
    }

    @Override
    public boolean validate() {
      return super.validate() && path != null;
    }
  }
}
