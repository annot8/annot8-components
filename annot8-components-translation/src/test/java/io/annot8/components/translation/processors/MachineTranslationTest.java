package io.annot8.components.translation.processors;

import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.content.TestStringContent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.dstl.machinetranslation.connector.api.EngineDetails;
import uk.gov.dstl.machinetranslation.connector.api.MTConnectorApi;
import uk.gov.dstl.machinetranslation.connector.api.Translation;
import uk.gov.dstl.machinetranslation.connector.api.exceptions.ConnectorException;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class MachineTranslationTest {
  @Test
  public void testProcessor() throws ConnectorException {
    MTConnectorApi mockConnector = Mockito.mock(MTConnectorApi.class);
    when(mockConnector.translate("fr", "en", "Bonjour le monde!")).thenReturn(new Translation("fr", "Hello world!"));
    when(mockConnector.queryEngine()).thenReturn(new EngineDetails("Mock Connector", "1.0.0", true, false, true));

    MachineTranslation.Processor processor = new MachineTranslation.Processor("fr", "en", true, mockConnector);

    Item item = new TestItem();

    item.createContent(TestStringContent.class)
        .withData("Bonjour le monde!")
        .withProperty("example", "example")
        .save();

    processor.process(item);

    verify(mockConnector, times(1)).queryEngine();
    verify(mockConnector, times(1)).translate("fr", "en", "Bonjour le monde!");

    List<Content> contents = item.getContents().collect(Collectors.toList());
    assertEquals(2, contents.size());

    contents.forEach(c -> assertTrue(c.getProperties().has("example")));

    List<Content> translatedContents = contents.stream().filter(c -> c.getProperties().has(PropertyKeys.PROPERTY_KEY_LANGUAGE)).collect(Collectors.toList());
    assertEquals(1, translatedContents.size());
    assertEquals("Hello world!", translatedContents.get(0).getData());
    assertEquals("en", translatedContents.get(0).getProperties().get(PropertyKeys.PROPERTY_KEY_LANGUAGE).get());
  }

  @Test
  public void testProcessorNoCopy() throws ConnectorException {
    MTConnectorApi mockConnector = Mockito.mock(MTConnectorApi.class);
    when(mockConnector.translate("fr", "en", "Bonjour le monde!")).thenReturn(new Translation("fr", "Hello world!"));
    when(mockConnector.queryEngine()).thenReturn(new EngineDetails("Mock Connector", "1.0.0", true, false, true));

    MachineTranslation.Processor processor = new MachineTranslation.Processor("fr", "en", false, mockConnector);

    Item item = new TestItem();

    item.createContent(TestStringContent.class)
        .withData("Bonjour le monde!")
        .withProperty("example", "example")
        .save();

    processor.process(item);

    verify(mockConnector, times(1)).queryEngine();
    verify(mockConnector, times(1)).translate("fr", "en", "Bonjour le monde!");

    List<Content> translatedContents = item.getContents().filter(c -> c.getProperties().has(PropertyKeys.PROPERTY_KEY_LANGUAGE)).collect(Collectors.toList());
    assertEquals(1, translatedContents.size());
    assertEquals(1, translatedContents.get(0).getProperties().getAll().size());
    assertEquals("en", translatedContents.get(0).getProperties().get(PropertyKeys.PROPERTY_KEY_LANGUAGE).get());
  }

  //TODO: Test engine configuration
}
