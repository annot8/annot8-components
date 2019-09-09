/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.annot8.core.context.Context;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.Annot8Exception;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;

public class AbstractContentProcessorTest {

  private static final String SHOULD_PROCESS = "SHOULD_PROCESS";
  private static final String NOT_PROCESS = "NOT_PROCESS";

  @Test
  public void testProcessItemWithSettings() {
    AbstractContentProcessor.ContentAnnotatorSettings settings =
        Mockito.mock(AbstractContentProcessor.ContentAnnotatorSettings.class);
    when(settings.validate()).thenReturn(true);
    Context context = Mockito.mock(Context.class);
    doReturn(Optional.of(settings)).when(context).getSettings(Mockito.any());
    when(settings.getContent()).thenReturn(Collections.singleton(SHOULD_PROCESS));
    Item item = getMockedItem();

    TestcontentProcessor processor = new TestcontentProcessor();
    try {
      processor.configure(context);
    } catch (MissingResourceException | BadConfigurationException e) {
      fail("Test should not error here");
    }

    processor.processItem(item);
    assertEquals(1, processor.getObservedContent().size());
    assertEquals(SHOULD_PROCESS, processor.getObservedContent().get(0));
  }

  public void testProcessItemWithAcceptsContent() {
    AcceptingContentProcessor processor = new AcceptingContentProcessor();
    Item item = getMockedItem();
    processor.processItem(item);
    List<String> observedContent = processor.getObservedContent();
    assertEquals(1, observedContent.size());
    assertEquals(SHOULD_PROCESS, observedContent.get(0));
  }

  @Test
  public void testConfigureWithNonValidSettings() {
    AbstractContentProcessor.ContentAnnotatorSettings settings =
        Mockito.mock(AbstractContentProcessor.ContentAnnotatorSettings.class);
    when(settings.validate()).thenReturn(false);
    Context context = Mockito.mock(Context.class);
    doReturn(Optional.of(settings)).when(context).getSettings(Mockito.any());

    TestcontentProcessor processor = new TestcontentProcessor();
    assertThrows(BadConfigurationException.class, () -> processor.configure(context));
  }

  @Test
  public void testProcessingError() {
    Item item = Mockito.mock(Item.class);
    Content content = Mockito.mock(Content.class);
    doReturn(Stream.of(content)).when(item).getContents();
    AbstractContentProcessor processor = new ErrorContentProcessor();
    assertTrue(processor.processItem(item));
  }

  @Test
  public void testContentAnnotatorSettings() {
    AbstractContentProcessor.ContentAnnotatorSettings settings =
        new AbstractContentProcessor.ContentAnnotatorSettings(
            Collections.singleton(SHOULD_PROCESS));

    assertEquals(1, settings.getContent().size());
    assertTrue(settings.getContent().contains(SHOULD_PROCESS));
    assertTrue(settings.validate());
  }

  @Test
  public void testEmptyContentAnnotatorSettings() {
    AbstractContentProcessor.ContentAnnotatorSettings settings =
        new AbstractContentProcessor.ContentAnnotatorSettings(null);

    assertTrue(settings.getContent().isEmpty());
  }

  private Item getMockedItem() {
    Item item = Mockito.mock(Item.class);
    Content toProcess = Mockito.mock(Content.class);
    Content notProcess = Mockito.mock(Content.class);
    when(toProcess.getName()).thenReturn(SHOULD_PROCESS);
    when(notProcess.getName()).thenReturn(NOT_PROCESS);
    doAnswer(getAnswer(toProcess)).when(item).getContentByName(Mockito.eq(SHOULD_PROCESS));
    doAnswer(getAnswer(notProcess)).when(item).getContentByName(Mockito.eq(NOT_PROCESS));
    return item;
  }

  @SafeVarargs
  private <T> Answer<Stream<T>> getAnswer(T... content) {
    return new Answer<>() {
      @Override
      public Stream<T> answer(InvocationOnMock invocation) {
        return Stream.of(content);
      }
    };
  }

  private class TestcontentProcessor extends AbstractContentProcessor {

    private final List<String> observedContent = new ArrayList<>();

    @Override
    protected void processContent(Item item, Content<?> content) {
      observedContent.add(content.getName());
    }

    public List<String> getObservedContent() {
      return observedContent;
    }
  }

  private class AcceptingContentProcessor extends TestcontentProcessor {
    @Override
    protected boolean acceptsContent(Content<?> content) {
      return content.getName().equals(SHOULD_PROCESS);
    }
  }

  private class ErrorContentProcessor extends AbstractContentProcessor {
    @Override
    protected void processContent(Item item, Content<?> content) throws Annot8Exception {
      throw new Annot8Exception("Test throws error");
    }
  }
}
