package io.annot8.components.translation.processors;

import io.annot8.api.exceptions.BadConfigurationException;
import org.junit.jupiter.api.Test;
import uk.gov.dstl.machinetranslation.connector.api.*;
import uk.gov.dstl.machinetranslation.connector.api.exceptions.ConnectorException;
import uk.gov.dstl.machinetranslation.connector.api.utils.ConnectorUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MachineTranslationSettingsTest {
  @Test
  public void testSourceLanguage(){
    MachineTranslationSettings settings = new MachineTranslationSettings();

    assertEquals(ConnectorUtils.LANGUAGE_AUTO, settings.getSourceLanguage());

    settings.setSourceLanguage("FR");
    assertEquals("FR", settings.getSourceLanguage());

    settings.setSourceLanguage(null);
    assertEquals(ConnectorUtils.LANGUAGE_AUTO, settings.getSourceLanguage());
  }

  @Test
  public void testTargetLanguage(){
    MachineTranslationSettings settings = new MachineTranslationSettings();

    settings.setTargetLanguage("DE");
    assertEquals("DE", settings.getTargetLanguage());
  }

  @Test
  public void testTranslatorConfiguration(){
    MachineTranslationSettings settings = new MachineTranslationSettings();

    Map<String, Object> defaultConf = settings.getTranslatorConfiguration();
    assertNotNull(defaultConf);
    assertTrue(defaultConf.isEmpty());

    settings.setTranslatorConfiguration("url", "http://www.example.com");
    Map<String, Object> addedConf = settings.getTranslatorConfiguration();
    assertNotNull(addedConf);
    assertEquals(1, addedConf.size());
    assertTrue(addedConf.containsKey("url"));
    assertEquals("http://www.example.com", addedConf.get("url"));

    Map<String, Object> exampleConf = new HashMap<>();
    exampleConf.put("confidence", 0.75);
    settings.setTranslatorConfiguration(exampleConf);
    Map<String, Object> setConf = settings.getTranslatorConfiguration();
    assertNotNull(setConf);
    assertEquals(exampleConf, setConf);

    settings.setTranslatorConfiguration(null);
    Map<String, Object> nullConf = settings.getTranslatorConfiguration();
    assertNotNull(nullConf);
    assertTrue(nullConf.isEmpty());

    settings.setTranslatorConfiguration("url", "http://www.example.com");
    Map<String, Object> addedNullConf = settings.getTranslatorConfiguration();
    assertNotNull(addedNullConf);
    assertEquals(1, addedNullConf.size());
    assertTrue(addedNullConf.containsKey("url"));
    assertEquals("http://www.example.com", addedNullConf.get("url"));
  }

  @Test
  public void testCopyProperties(){
    MachineTranslationSettings settings = new MachineTranslationSettings();

    assertFalse(settings.isCopyProperties());

    settings.setCopyProperties(true);
    assertTrue(settings.isCopyProperties());
  }

  @Test
  public void testTranslatorClass(){
    MachineTranslationSettings settings = new MachineTranslationSettings();

    assertNull(settings.getTranslatorClass());

    settings.setTranslatorClass(NoOpConnector.class);
    assertEquals(NoOpConnector.class, settings.getTranslatorClass());

    assertThrows(BadConfigurationException.class, () -> settings.setTranslatorClass("foo bar baz"));
    assertThrows(BadConfigurationException.class, () -> settings.setTranslatorClass("java.lang.String"));

    settings.setTranslatorClass((Class<? extends MTConnectorApi>) null);
    assertNull(settings.getTranslatorClass());

    settings.setTranslatorClass(NoOpConnector.class.getName());
    assertEquals(NoOpConnector.class, settings.getTranslatorClass());
  }

  @Test
  public void testValidate(){
    MachineTranslationSettings settings = new MachineTranslationSettings();

    assertFalse(settings.validate());

    settings.setTargetLanguage("EN");
    settings.setTranslatorClass(NoOpConnector.class);
    assertTrue(settings.validate());

    settings.setTargetLanguage("EN");
    settings.setTranslatorClass((Class<? extends MTConnectorApi>) null);
    assertFalse(settings.validate());

    settings.setTargetLanguage(null);
    settings.setTranslatorClass(NoOpConnector.class);
    assertFalse(settings.validate());
  }

  private static class NoOpConnector implements MTConnectorApi{
    @Override
    public void configure(Map<String, Object> configuration) {
      //Do nothing
    }

    @Override
    public Collection<LanguagePair> supportedLanguages() {
      return Collections.emptyList();
    }

    @Override
    public List<LanguageDetection> identifyLanguage(String content)  {
      throw new UnsupportedOperationException("NoOpConnector does not support this function");
    }

    @Override
    public Translation translate(String sourceLanguage, String targetLanguage, String content) throws ConnectorException {
      return new Translation("UNK", content);
    }

    @Override
    public EngineDetails queryEngine() {
      return new EngineDetails("NoOpConnector", "1.0.0", true, false, true);
    }
  }
}
