/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8RuntimeException;

public class AbstractItemProcessorTest {

  @Test
  public void testProcess() {
    Item item = Mockito.mock(Item.class);
    AbstractItemProcessor processor = new TestItemProcessor(true);
    ProcessorResponse response = processor.process(item);
    assertEquals(ProcessorResponse.Status.OK, response.getStatus());
    Mockito.verify(item, Mockito.times(0)).discard();
  }

  @Test
  public void testNonProcess() {
    Item item = Mockito.mock(Item.class);
    AbstractItemProcessor processor = new TestItemProcessor(false);
    ProcessorResponse response = processor.process(item);
    assertEquals(ProcessorResponse.Status.OK, response.getStatus());
    Mockito.verify(item, Mockito.times(1)).discard();
  }

  @Test
  public void testErrorInProcess() {
    Item item = Mockito.mock(Item.class);
    AbstractItemProcessor processor = new ErrorItemProcessor();
    ProcessorResponse response = processor.process(item);
    assertEquals(ProcessorResponse.Status.ITEM_ERROR, response.getStatus());
  }

  private class TestItemProcessor extends AbstractItemProcessor {

    private final boolean doesProcess;

    public TestItemProcessor(boolean doesProcess) {
      this.doesProcess = doesProcess;
    }

    @Override
    protected boolean acceptsItem(Item item) {
      return true;
    }

    @Override
    protected boolean processItem(Item item) {
      return doesProcess;
    }
  }

  private class ErrorItemProcessor extends TestItemProcessor {

    public ErrorItemProcessor() {
      super(true);
    }

    @Override
    protected boolean processItem(Item item) {
      throw new Annot8RuntimeException("Test throws an exception");
    }
  }
}
