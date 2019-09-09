/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.components.responses.ProcessorResponse.Status;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8RuntimeException;

public class AbstractFilterTest {

  @Test
  public void testFilterItems() {
    FilterAll filter = new FilterAll();
    Item item = Mockito.mock(Item.class);
    ProcessorResponse processResponse = filter.process(item);
    assertEquals(Status.OK, processResponse.getStatus());
    Mockito.verify(item, Mockito.times(1)).discard();
  }

  @Test
  public void testFilterError() {
    FilterError error = new FilterError();
    ProcessorResponse response = error.process(Mockito.mock(Item.class));
    assertEquals(ProcessorResponse.Status.ITEM_ERROR, response.getStatus());
  }

  private class FilterAll extends AbstractFilter {

    @Override
    protected boolean filter(Item item) {
      return true;
    }
  }

  private class FilterError extends AbstractFilter {

    @Override
    protected boolean filter(Item item) {
      throw new Annot8RuntimeException("Test should throw this error");
    }
  }
}
