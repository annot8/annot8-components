/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tesseract.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.commons.io.FilenameUtils;

import java.util.*;

/**
 * Takes FileContent containing either an image or PDF file, and produces a Text content with the
 * text from the file as extracted by Tesseract
 */
@ComponentName("Tesseract OCR")
@ComponentDescription("Use Tesseract to extract text from images stored in FileContent")
@SettingsClass(OCR.Settings.class)
public class OCR extends AbstractProcessorDescriptor<OCR.Processor, OCR.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    ITesseract instance = new Tesseract();

    if (!settings.getConfigs().isEmpty())
      instance.setConfigs(settings.getConfigs());

    instance.setDatapath(settings.getDataPath());
    instance.setLanguage(settings.getLanguage());
    instance.setOcrEngineMode(settings.getOcrEngine());
    instance.setPageSegMode(settings.getPageSegmentation());
    settings.getVariables().forEach(instance::setTessVariable);

    return new Processor(settings.getExtensions(), instance);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(FileContent.class)
        .withCreatesContent(Text.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    private final ITesseract instance;
    private final List<String> extensions;

    public Processor(List<String> extensions, ITesseract tesseract) {
      this.extensions = extensions;
      this.instance = tesseract;
    }

    @Override
    public ProcessorResponse process(Item item) {

      item.getContents(FileContent.class)
          .filter(
              fc ->
                  extensions
                      .contains(FilenameUtils.getExtension(fc.getData().getName()).toLowerCase()))
          .forEach(
              fc -> {
                try {
                  String content = instance.doOCR(fc.getData());

                  item.createContent(Text.class)
                      .withDescription("OCR from " + fc.getId())
                      .withData(content)
                      .withProperties(fc.getProperties())
                      .save();
                } catch (TesseractException e) {
                  log().error("Unable to extract text from content {}", fc.getId(), e);
                }
              });

      return ProcessorResponse.ok();
    }
  }

  /**
   * Settings class for {@link OCR}
   */
  public static class Settings implements io.annot8.api.settings.Settings {
    //Processor Settings
    private List<String> extensions = Arrays.asList("bmp", "gif", "jpg", "jpeg", "pdf", "tif", "tiff");

    //Tesseract Settings
    private List<String> configs = new ArrayList<>();
    private String dataPath = LoadLibs.extractTessResources("tessdata").toString();
    private String language = "eng";
    private int ocrEngine = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
    private int pageSegmentation = -1;
    private Map<String, String> variables = new HashMap<>();

    public List<String> getExtensions() {
      return extensions;
    }
    public void setExtensions(List<String> extensions) {
      this.extensions = extensions;
    }

    public List<String> getConfigs() {
      return configs;
    }
    public void setConfigs(List<String> configs) {
      this.configs = configs;
    }

    public String getDataPath() {
      return dataPath;
    }
    public void setDataPath(String dataPath) {
      this.dataPath = dataPath;
    }

    public String getLanguage() {
      return language;
    }
    public void setLanguage(String language) {
      this.language = language;
    }

    public int getOcrEngine() {
      return ocrEngine;
    }
    public void setOcrEngine(int ocrEngine) {
      this.ocrEngine = ocrEngine;
    }

    public int getPageSegmentation() {
      return pageSegmentation;
    }
    public void setPageSegmentation(int pageSegmentation) {
      this.pageSegmentation = pageSegmentation;
    }

    public Map<String, String> getVariables() {
      return variables;
    }
    public void setVariables(Map<String, String> variables) {
      this.variables = variables;
    }

    @Override
    public boolean validate() {
      return extensions != null
          && !extensions.isEmpty()
          && configs != null
          && dataPath != null
          && !dataPath.isEmpty()
          && language != null
          && !language.isEmpty()
          && variables != null;
    }
  }
}