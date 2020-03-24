package io.annot8.components.translation.processors;

import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;
import uk.gov.dstl.machinetranslation.connector.api.MTConnectorApi;
import uk.gov.dstl.machinetranslation.connector.api.utils.ConnectorUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MachineTranslationSettings implements Settings {
  private String sourceLanguage = ConnectorUtils.LANGUAGE_AUTO;
  private String targetLanguage = null;
  private Class<? extends MTConnectorApi> translatorClass = null;
  private Map<String, Object> translatorConfiguration = new HashMap<>();

  private boolean copyProperties = false;

  @Override
  public boolean validate() {
    return targetLanguage != null && translatorClass != null;
  }

  @Description(value = "Source language to translate from, or "+ConnectorUtils.LANGUAGE_AUTO, defaultValue = ConnectorUtils.LANGUAGE_AUTO)
  public String getSourceLanguage() {
    if(sourceLanguage == null)
      return ConnectorUtils.LANGUAGE_AUTO;

    return sourceLanguage;
  }
  public void setSourceLanguage(String sourceLanguage) {
    this.sourceLanguage = sourceLanguage;
  }

  @Description("Target language to translate to")
  public String getTargetLanguage() {
    return targetLanguage;
  }
  public void setTargetLanguage(String targetLanguage) {
    this.targetLanguage = targetLanguage;
  }

  @Description("MTConnectorApi Connector Class")
  public Class<? extends MTConnectorApi> getTranslatorClass() {
    return translatorClass;
  }
  public void setTranslatorClass(Class<? extends MTConnectorApi> translatorClass) {
    this.translatorClass = translatorClass;
  }
  public void setTranslatorClass(String translatorClass) throws BadConfigurationException {
    Class<?> clazz;
    try {
      clazz = Class.forName(translatorClass);
    } catch (ClassNotFoundException e) {
      this.translatorClass = null;
      throw new BadConfigurationException("Could not find class "+translatorClass, e);
    }

    if(!MTConnectorApi.class.isAssignableFrom(clazz)) {
      this.translatorClass = null;
      throw new BadConfigurationException("Translator Class must implement MTConnectorApi");
    }

    this.translatorClass = (Class<? extends MTConnectorApi>) clazz;
  }

  @Description("Configuration for Connector API")
  public Map<String, Object> getTranslatorConfiguration() {
    if(translatorConfiguration == null)
      return Collections.emptyMap();

    return translatorConfiguration;
  }
  public void setTranslatorConfiguration(Map<String, Object> translatorConfiguration) {
    this.translatorConfiguration = translatorConfiguration;
  }
  public void setTranslatorConfiguration(String key, Object value){
    if (translatorConfiguration == null)
      translatorConfiguration = new HashMap<>();

    translatorConfiguration.put(key, value);
  }

  @Description(value = "Should properties be copied from source Content?", defaultValue = "false")
  public boolean isCopyProperties() {
    return copyProperties;
  }
  public void setCopyProperties(boolean copyProperties) {
    this.copyProperties = copyProperties;
  }
}
