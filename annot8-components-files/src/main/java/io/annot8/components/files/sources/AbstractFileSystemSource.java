/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.annot8.api.components.Source;
import io.annot8.api.data.Item;
import io.annot8.api.data.ItemFactory;
import io.annot8.common.components.AbstractComponent;
import io.annot8.common.data.content.FileContent;
import io.annot8.conventions.PropertyKeys;

// @CreatesContent(FileContent.class)
public abstract class AbstractFileSystemSource extends AbstractComponent implements Source {

  private Set<Pattern> acceptedFilePatterns = Collections.emptySet();
  private FileSystemSourceSettings settings;

  AbstractFileSystemSource(FileSystemSourceSettings settings) {
    acceptedFilePatterns = settings.getAcceptedFileNamePatterns();
    this.settings = settings;
  }

  protected FileSystemSourceSettings getSettings() {
    return settings;
  }

  protected Set<Pattern> getAcceptedFilePatterns() {
    return acceptedFilePatterns;
  }

  protected boolean createItem(ItemFactory itemFactory, Path path) {
    boolean include = false;

    if (getAcceptedFilePatterns().isEmpty()) {
      include = true;
    } else {
      for (Pattern p : getAcceptedFilePatterns()) {
        Matcher m = p.matcher(path.getFileName().toString());
        if (m.matches()) {
          include = true;
          break;
        }
      }
    }

    if (include) {
      final Item item = itemFactory.create();
      try {
        item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, path);
        item.getProperties()
            .set(PropertyKeys.PROPERTY_KEY_ACCESSEDAT, Instant.now().getEpochSecond());

        item.createContent(FileContent.class)
            .withDescription("File " + path.toString())
            .withData(path.toFile())
            .save();

        return true;
      } catch (Throwable t) {
        log().error("Unable to create item, discarding", t);
        item.discard();
      }
    }

    return false;
  }
}
