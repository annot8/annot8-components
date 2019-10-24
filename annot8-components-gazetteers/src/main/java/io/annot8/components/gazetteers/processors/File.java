/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import java.nio.file.Path;

import io.annot8.api.context.Context;
import io.annot8.components.gazetteers.processors.impl.FileGazetteer;

public class File extends AhoCorasick<File.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(new FileGazetteer(settings.getPath(), settings.getSeparator()), settings);
  }

  public static class Settings extends AhoCorasick.Settings {
    private Path path;
    private char separator = ',';

    public Path getPath() {
      return path;
    }

    public void setPath(Path path) {
      this.path = path;
    }

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
