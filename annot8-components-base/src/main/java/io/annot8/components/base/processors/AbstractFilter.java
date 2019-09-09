/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;

/** Base class for building filters which discard files based on some criteria */
public abstract class AbstractFilter extends AbstractComponent implements Processor {

  @Override
  public final ProcessorResponse process(final Item item) {
    metrics().counter("process.called").increment();

    try {
      if (filter(item)) {
        metrics().counter("items.filtered").increment();
        item.discard();
      }

      return ProcessorResponse.ok();
    } catch (final Exception e) {
      metrics().counter("items.errors").increment();

      return ProcessorResponse.itemError();
    }
  }

  /**
   * Should the item be discarded from the pipeline?
   *
   * @param item the item to test
   * @return false if the item should be kept
   */
  protected abstract boolean filter(final Item item);
}
