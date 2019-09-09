/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.annot8.common.data.content.FileContent;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.conventions.PropertyKeys;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.components.Source;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.data.ItemFactory;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;

@CreatesContent(FileContent.class)
public abstract class AbstractFileSystemSource extends AbstractComponent implements Source {

  private Set<Pattern> acceptedFilePatterns = Collections.emptySet();

  private FileSystemSourceSettings settings;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    final Optional<FileSystemSourceSettings> optional =
        context.getSettings(FileSystemSourceSettings.class);

    if (!optional.isPresent()) {
      throw new BadConfigurationException("File system settings are invalid");
    }

    settings = optional.get();
    acceptedFilePatterns = settings.getAcceptedFileNamePatterns();
  }

  public FileSystemSourceSettings getSettings() {
    return settings;
  }

  public Set<Pattern> getAcceptedFilePatterns() {
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

        item.create(FileContent.class).withName("file").withData(path.toFile()).save();

        return true;
      } catch (Throwable t) {
        log().error("Unable to create item, discarding", t);
        item.discard();
      }
    }

    return false;
  }
}
