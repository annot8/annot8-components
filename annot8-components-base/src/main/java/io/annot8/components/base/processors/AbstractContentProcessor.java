/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import io.annot8.components.base.processors.AbstractContentProcessor.ContentAnnotatorSettings;
import io.annot8.core.context.Context;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.Settings;
import io.annot8.core.settings.SettingsClass;

/** A base class for content processors which limit to content by (configurable name) */
@SettingsClass(ContentAnnotatorSettings.class)
public abstract class AbstractContentProcessor extends AbstractItemProcessor {

  private ContentAnnotatorSettings settings;

  @Override
  public void configure(final Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);

    settings = context.getSettings(ContentAnnotatorSettings.class).orElse(null);

    if (settings != null && !settings.validate()) {
      throw new BadConfigurationException("Invalid content settings");
    }
  }

  @Override
  protected final boolean processItem(final Item item) {

    Stream<Content<?>> contentToProcess;
    // Did we limit the views?
    if (settings == null || settings.getContent() == null || settings.getContent().isEmpty()) {
      contentToProcess = item.getContents();
    } else {
      contentToProcess = settings.getContent().stream().flatMap(item::getContentByName);
    }

    contentToProcess
        .filter(this::acceptsContent)
        .forEach(
            c -> {
              try {
                metrics().counter("content.accepted").increment();
                processContent(item, c);
              } catch (Annot8Exception e) {
                metrics().counter("content.errors").increment();
                log().warn("Unable to process content {}", c.getName(), e);
              }
            });

    // Always pass on to next
    return true;
  }

  /**
   * Check if the content should be passed to processContent
   *
   * @param content the content to be processed
   * @return true is the content should be processed
   */
  protected boolean acceptsContent(final Content<?> content) {
    return true;
  }

  /**
   * Process the content
   *
   * @param item the owning item
   * @param content the content to provess
   * @throws Annot8Exception if the content can't be processed
   */
  protected abstract void processContent(final Item item, final Content<?> content)
      throws Annot8Exception;

  public static class ContentAnnotatorSettings implements Settings {

    // List of view name to consider
    // null/empty implies all
    private final Set<String> content;

    public ContentAnnotatorSettings(Set<String> content) {
      this.content = content;
    }

    public Set<String> getContent() {
      return content == null ? Collections.emptySet() : content;
    }

    @Override
    public boolean validate() {
      return true;
    }
  }
}
