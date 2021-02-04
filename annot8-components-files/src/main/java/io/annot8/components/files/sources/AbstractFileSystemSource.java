/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import io.annot8.api.data.Item;
import io.annot8.api.data.ItemFactory;
import io.annot8.common.components.AbstractSource;
import io.annot8.common.data.content.FileContent;
import io.annot8.conventions.PropertyKeys;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractFileSystemSource extends AbstractSource {

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
    return createItem(itemFactory, path, 0L);
  }

  protected boolean createItem(ItemFactory itemFactory, Path path, long delay) {
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
      log().debug("Scheduling item creation for {} after delay of {} milliseconds", path, delay);

      new Timer()
          .schedule(
              new TimerTask() {
                @Override
                public void run() {
                  log().debug("Creating item for {}", path);
                  final Item item = itemFactory.create();
                  try {
                    item.getProperties().set(PropertyKeys.PROPERTY_KEY_SOURCE, path);
                    item.getProperties()
                        .set(PropertyKeys.PROPERTY_KEY_ACCESSEDAT, Instant.now().getEpochSecond());

                    item.createContent(FileContent.class)
                        .withDescription("File " + path.toString())
                        .withData(path.toFile())
                        .save();
                  } catch (Throwable t) {
                    log().warn("Unable to create item, discarding", t);
                    item.discard();
                  }
                }
              },
              delay);

      return true;
    } else {
      return false;
    }
  }
}
