/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import io.annot8.api.data.Item;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.data.content.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AbstractContentProcessorTest {

  private static final String SHOULD_PROCESS = "SHOULD_PROCESS";
  private static final String NOT_PROCESS = "NOT_PROCESS";

  @Test
  public void testProcessItem() {
    Item item = getMockedItem();

    try (TestContentProcessor processor = new TestContentProcessor()) {
      processor.process(item);
      assertEquals(2, processor.getObservedContent().size());
      assertEquals(SHOULD_PROCESS, processor.getObservedContent().get(0));
      assertEquals(NOT_PROCESS, processor.getObservedContent().get(1));
    }
  }

  @Test
  public void testAcceptsContent() {
    try (AcceptingContentProcessor processor = new AcceptingContentProcessor()) {
      Item item = getMockedItem();
      processor.process(item);
      List<String> observedContent = processor.getObservedContent();
      assertEquals(1, observedContent.size());
      assertEquals(SHOULD_PROCESS, observedContent.get(0));
    }
  }

  @Test
  public void testProcessingError() {
    Item item = getMockedItem();
    Text content = Mockito.mock(Text.class);
    doReturn(Stream.of(content)).when(item).getContents();
    try (AbstractContentProcessor<Text> processor = new ErrorContentProcessor()) {
      assertTrue(processor.process(item).hasExceptions());
    }
  }

  private Item getMockedItem() {
    Item item = Mockito.mock(Item.class);
    Text toProcess = Mockito.mock(Text.class);
    Text notProcess = Mockito.mock(Text.class);
    when(toProcess.getData()).thenReturn(SHOULD_PROCESS);
    when(notProcess.getData()).thenReturn(NOT_PROCESS);
    when(item.getContents(Text.class)).thenReturn(Stream.of(toProcess, notProcess));

    return item;
  }

  private class TestContentProcessor extends AbstractContentProcessor<Text> {

    private final List<String> observedContent = new ArrayList<>();

    public TestContentProcessor() {
      super(Text.class);
    }

    @Override
    protected void process(Text content) {
      observedContent.add(content.getData());
    }

    public List<String> getObservedContent() {
      return observedContent;
    }
  }

  private class AcceptingContentProcessor extends TestContentProcessor {
    @Override
    protected boolean accept(Text content) {
      return content.getData().equals(SHOULD_PROCESS);
    }
  }

  private class ErrorContentProcessor extends AbstractContentProcessor<Text> {

    public ErrorContentProcessor() {
      super(Text.class);
    }

    @Override
    protected void process(Text content) {
      throw new ProcessingException("Test throws error");
    }
  }
}
