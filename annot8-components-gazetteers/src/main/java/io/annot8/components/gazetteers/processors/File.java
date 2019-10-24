/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.annot8.components.gazetteers.processors;

import io.annot8.api.context.Context;
import io.annot8.components.gazetteers.processors.impl.FileGazetteer;

import java.nio.file.Path;

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
