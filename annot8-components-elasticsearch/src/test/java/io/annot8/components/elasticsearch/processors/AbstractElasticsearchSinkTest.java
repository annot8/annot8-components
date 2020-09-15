/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.testing.testimpl.TestItem;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AbstractElasticsearchSinkTest {
  @Test
  public void testEmptyItem() {
    AbstractElasticsearchSink sink =
        mock(AbstractElasticsearchSink.class, Mockito.CALLS_REAL_METHODS);

    TestItem item = new TestItem();

    when(sink.itemToIndexRequests(any())).thenReturn(Collections.emptyList());

    ProcessorResponse response = sink.process(item);
    assertEquals(ProcessorResponse.ok(), response);

    verify(sink, times(1)).itemToIndexRequests(item);
  }
}
