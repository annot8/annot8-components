/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tesseract.processors.settings;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.util.LoadLibs;

import io.annot8.core.settings.Settings;

public class TesseractSettings implements Settings {

  private final List<String> configs;
  private final String dataPath;
  private final String language;
  private final int ocrEngine;
  private final int pageSegmentation;
  private final Map<String, String> variables;

  private TesseractSettings(
      List<String> configs,
      String dataPath,
      String language,
      int ocrEngine,
      int pageSegmentation,
      Map<String, String> variables) {
    // Private constructor - use builder
    this.dataPath = dataPath;
    this.language = language;
    this.ocrEngine = ocrEngine;
    this.pageSegmentation = pageSegmentation;
    this.configs = configs;
    this.variables = variables;
  }

  public List<String> getConfigs() {
    return configs;
  }

  public String getDataPath() {
    return dataPath;
  }

  public String getLanguage() {
    return language;
  }

  public int getOcrEngine() {
    return ocrEngine;
  }

  public int getPageSegmentation() {
    return pageSegmentation;
  }

  public Map<String, String> getVariables() {
    return variables;
  }

  public String getVariable(String key) {
    return variables.get(key);
  }

  public String getVariable(String key, String defaultValue) {
    return variables.getOrDefault(key, defaultValue);
  }

  public void configureInstance(ITesseract instance) {
    if (!configs.isEmpty()) instance.setConfigs(configs);
    instance.setDatapath(dataPath);
    instance.setLanguage(language);
    instance.setOcrEngineMode(ocrEngine);
    instance.setPageSegMode(pageSegmentation);
    variables.forEach(instance::setTessVariable);
  }

  @Override
  public boolean validate() {
    return configs != null
        && dataPath != null
        && !dataPath.isEmpty()
        && language != null
        && !language.isEmpty()
        && variables != null;
  }

  public static class Builder {
    private List<String> configs = new ArrayList<>();
    private String dataPath = null;
    private String language = "eng";
    private int ocrEngine = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
    private int pageSegmentation = -1;
    private Map<String, String> variables = new HashMap<>();

    public TesseractSettings build() {
      return new TesseractSettings(
          configs, dataPath, language, ocrEngine, pageSegmentation, variables);
    }

    public Builder withConfig(String config) {
      this.configs.add(config);
      return this;
    }

    public Builder withConfigs(List<String> configs) {
      this.configs = configs;
      return this;
    }

    public Builder withDataPath(String dataPath) {
      this.dataPath = dataPath;
      return this;
    }

    public Builder withDataPath(Path dataPath) {
      this.dataPath = dataPath.toString();
      return this;
    }

    public Builder withDefaultDataPath() {
      this.dataPath = LoadLibs.extractTessResources("tessdata").toString();
      return this;
    }

    public Builder withLanguage(String language) {
      this.language = language;
      return this;
    }

    public Builder withOcrEngine(int ocrEngine) {
      this.ocrEngine = ocrEngine;
      return this;
    }

    public Builder withPageSegmentation(int pageSegmentation) {
      this.pageSegmentation = pageSegmentation;
      return this;
    }

    public Builder withVariable(String key, String value) {
      this.variables.put(key, value);
      return this;
    }

    public Builder withVariables(Map<String, String> variables) {
      this.variables = variables;
      return this;
    }

    public Builder mergeVariables(Map<String, String> variables) {
      this.variables.putAll(variables);
      return this;
    }
  }
}
