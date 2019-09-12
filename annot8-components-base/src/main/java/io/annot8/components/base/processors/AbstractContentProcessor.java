/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import java.util.LinkedList;
import java.util.List;

import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.capabilities.Capabilities;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;

/**
 * A base class for building processors which act on a specific class of content
 *
 * <p>This is likely to be the base class for many processors which will function and requrie on
 * specific content types
 *
 * <p>All content is processed if it has the correct class. Any exceptions thrown are collated and
 * returned together (after other content has been processed).
 *
 * @param <T> the content class processed
 */
public abstract class AbstractContentProcessor<T extends Content<?>> extends AbstractComponent
    implements Processor {

  private final Class<T> contentClazz;

  /**
   * New instances
   *
   * @param contentClazz the content to process
   */
  protected AbstractContentProcessor(final Class<T> contentClazz) {
    this.contentClazz = contentClazz;
  }

  @Override
  public ProcessorResponse process(Item item) {
    List<Exception> exceptions = new LinkedList<>();

    item.getContents(contentClazz)
        .filter(this::accept)
        .forEach(
            c -> {
              try {
                process(c);
              } catch (Exception e) {
                exceptions.add(e);
              }
            });

    return exceptions.isEmpty() ? ProcessorResponse.ok() : ProcessorResponse.itemError(exceptions);
  }

  /**
   * Should this content be processed?
   *
   * <p>Default to accepting all content.
   *
   * @param content the content to process
   * @return true is content should be processed
   */
  protected boolean accept(final T content) {
    return true;
  }

  /**
   * Process the content
   *
   * @param content the content to process
   */
  protected abstract void process(final T content);

  @Override
  public void buildCapabilities(Capabilities.Builder builder) {
    super.buildCapabilities(builder);

    builder.processesContent(contentClazz, false);
  }
}
