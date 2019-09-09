/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;

/** Base class to simplify selectively processing itmem */
public abstract class AbstractItemProcessor extends AbstractComponent implements Processor {

  @Override
  public final ProcessorResponse process(final Item item) {

    try {
      metrics().counter("process.called").increment();

      if (acceptsItem(item)) {
        metrics().counter("items.accepted").increment();

        final boolean stop = processItem(item);
        if (!stop) {
          metrics().counter("items.discarded").increment();

          item.discard();
        }
      }
      return ProcessorResponse.ok();
    } catch (final Exception e) {
      metrics().counter("process.errors").increment();

      return ProcessorResponse.itemError();
    }
  }

  /**
   * Should this item be passed to processItem?
   *
   * @param item the item to test
   * @return true if the item should be processed
   */
  protected boolean acceptsItem(final Item item) {
    return true;
  }

  /**
   * Process the item
   *
   * @param item the item to process
   * @return false if the item should be discarded from the pipeline
   */
  protected abstract boolean processItem(final Item item);
}
