/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;

/** Base class for dividing items into multiple itemms */
public abstract class AbstractSplitter extends AbstractComponent implements Processor {

  @Override
  public final ProcessorResponse process(final Item item) {
    metrics().counter("process.called").increment();

    try {
      if (acceptsItem(item)) {
        metrics().counter("items.accepted").increment();

        boolean discard = split(item);
        if (discard) {
          metrics().counter("items.discarded").increment();
          item.discard();
        }
      }

      return ProcessorResponse.ok();
    } catch (final Exception e) {
      metrics().counter("items.errors").increment();
      return ProcessorResponse.itemError();
    }
  }

  /**
   * Should this item be considered for division?
   *
   * @param item the item
   * @return true is should be split
   */
  protected boolean acceptsItem(final Item item) {
    return false;
  }

  /**
   * Split (or attempt to split the item)
   *
   * @param item the item
   * @return true is the original item should now be discarded
   */
  protected abstract boolean split(final Item item);
}
