/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.components.AbstractProcessor;

public class MultiProcessor extends AbstractProcessor {

  private Collection<Processor> processors;

  public MultiProcessor() {
    this.processors = new ArrayList<>();
  }

  public MultiProcessor(Processor... processors) {
    this.processors = Arrays.asList(processors);
  }

  public MultiProcessor(Collection<Processor> processors) {
    this.processors = processors;
  }

  protected void addProcessor(Processor processor) {
    this.processors.add(processor);
  }

  @Override
  public ProcessorResponse process(Item item) {
    boolean error = false;
    Collection<Exception> exceptions = new ArrayList<>();

    for (Processor p : processors) {
      ProcessorResponse response = p.process(item);
      if (response.getStatus() != ProcessorResponse.Status.OK) {
        error = true;
        exceptions.addAll(response.getExceptions());
      }
    }

    if (error) {
      return ProcessorResponse.processingError(exceptions);
    }

    return ProcessorResponse.ok();
  }

  @Override
  public void close() {
    for (Processor p : processors) {
      p.close();
    }
  }
}
