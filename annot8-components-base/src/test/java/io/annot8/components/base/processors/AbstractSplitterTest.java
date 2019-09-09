/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8RuntimeException;

public class AbstractSplitterTest {

  @Test
  public void testProcessAcceptingItem() {
    Item item = Mockito.mock(Item.class);
    AbstractSplitter splitter = new Splitter();
    ProcessorResponse response = splitter.process(item);
    Mockito.verify(item, Mockito.times(1)).discard();
    assertEquals(ProcessorResponse.Status.OK, response.getStatus());
  }

  @Test
  public void testProcessNonAcceptingItem() {
    Item item = Mockito.mock(Item.class);
    AbstractSplitter splitter = new NonAcceptingSplitter();
    ProcessorResponse response = splitter.process(item);
    assertEquals(ProcessorResponse.Status.OK, response.getStatus());
    Mockito.verify(item, Mockito.times(0)).discard();
  }

  @Test
  public void testProcessError() {
    Item item = Mockito.mock(Item.class);
    AbstractSplitter splitter = new ErrorSplitter();
    ProcessorResponse response = splitter.process(item);
    assertEquals(ProcessorResponse.Status.ITEM_ERROR, response.getStatus());
  }

  private class Splitter extends AbstractSplitter {

    @Override
    protected boolean acceptsItem(Item item) {
      return true;
    }

    @Override
    protected boolean split(Item item) {
      return true;
    }
  }

  private class NonAcceptingSplitter extends AbstractSplitter {

    @Override
    protected boolean split(Item item) {
      return true;
    }
  }

  private class ErrorSplitter extends AbstractSplitter {

    @Override
    protected boolean acceptsItem(Item item) {
      return true;
    }

    @Override
    protected boolean split(Item item) {
      throw new Annot8RuntimeException("Testing error throwing");
    }
  }
}
