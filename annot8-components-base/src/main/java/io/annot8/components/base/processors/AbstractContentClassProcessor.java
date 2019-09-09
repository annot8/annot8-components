/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import io.annot8.core.capabilities.Capabilities;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;

/**
 * A base class for building processors which act on a specific class of content
 *
 * <p>This is likely to be the base class for many processors which will function and requrie on
 * specific content types
 *
 * @param <T> the content class processed
 */
public abstract class AbstractContentClassProcessor<T extends Content<?>>
    extends AbstractContentProcessor {

  private final Class<T> contentClazz;

  /**
   * New instances
   *
   * @param contentClazz the content to process
   */
  protected AbstractContentClassProcessor(final Class<T> contentClazz) {
    this.contentClazz = contentClazz;
  }

  @Override
  protected boolean acceptsContent(final Content<?> content) {
    return contentClazz.isInstance(content);
  }

  @Override
  protected void processContent(final Item item, final Content<?> content) throws Annot8Exception {
    // TODO: We could check the accepts here again before the cast but it should have been checked
    process(item, (T) content);
  }

  /**
   * Process the content
   *
   * @param item the owning item
   * @param content the content to provess
   * @throws Annot8Exception if unable to process
   */
  protected abstract void process(final Item item, final T content) throws Annot8Exception;

  @Override
  public void buildCapabilities(Capabilities.Builder builder) {
    super.buildCapabilities(builder);

    builder.processesContent(contentClazz, false);
  }
}
