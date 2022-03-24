/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.components.responses.ProcessorResponse.Status;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.Annot8RuntimeException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AbstractFilterProcessorTest {

  @Test
  void testFilterItems() {
    try (FilterAll filter = new FilterAll()) {
      Item item = Mockito.mock(Item.class);
      ProcessorResponse processResponse = filter.process(item);
      assertEquals(Status.OK, processResponse.getStatus());
      Mockito.verify(item, Mockito.times(1)).discard();
    }
  }

  @Test
  void testFilterError() {
    try (FilterError error = new FilterError()) {
      ProcessorResponse response = error.process(Mockito.mock(Item.class));
      assertEquals(ProcessorResponse.Status.ITEM_ERROR, response.getStatus());
    }
  }

  private class FilterAll extends AbstractFilterProcessor {

    @Override
    protected boolean filter(Item item) {
      return true;
    }
  }

  private class FilterError extends AbstractFilterProcessor {

    @Override
    protected boolean filter(Item item) {
      throw new Annot8RuntimeException("Test should throw this error");
    }
  }
}
