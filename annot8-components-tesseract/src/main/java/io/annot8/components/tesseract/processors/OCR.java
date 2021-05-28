/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tesseract.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import org.apache.commons.io.FilenameUtils;

/**
 * Takes FileContent containing either an image or PDF file, or Image content directly, and produces
 * a Text content with the text from the image as extracted by Tesseract
 */
@ComponentName("Tesseract OCR")
@ComponentDescription(
    "Use Tesseract to extract text from images stored in FileContent, or directly from Image content")
@SettingsClass(OCR.Settings.class)
@ComponentTags({"image", "text", "ocr", "tesseract"})
public class OCR extends AbstractProcessorDescriptor<OCR.Processor, OCR.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    ITesseract instance = new Tesseract();

    if (!settings.getConfigs().isEmpty()) instance.setConfigs(settings.getConfigs());

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
        .withProcessesContent(Image.class)
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
                  extensions.contains(
                      FilenameUtils.getExtension(fc.getData().getName()).toLowerCase()))
          .forEach(
              fc -> {
                String content =
                    metrics()
                        .timer("ocr-file")
                        .record(
                            () -> {
                              try {
                                return instance.doOCR(fc.getData());
                              } catch (TesseractException e) {
                                log()
                                    .error(
                                        "Unable to extract text from File content {}",
                                        fc.getId(),
                                        e);
                              }

                              return null;
                            });

                createTextContent(item, content, fc);
              });

      item.getContents(Image.class)
          .forEach(
              image -> {
                String content =
                    metrics()
                        .timer("ocr-image")
                        .record(
                            () -> {
                              try {
                                return instance.doOCR(image.getData());
                              } catch (TesseractException e) {
                                log()
                                    .error(
                                        "Unable to extract text from Image content {}",
                                        image.getId(),
                                        e);
                              }

                              return null;
                            });

                createTextContent(item, content, image);
              });

      return ProcessorResponse.ok();
    }

    private Text createTextContent(Item item, String textContent, Content<?> sourceContent) {
      if (textContent == null || textContent.isBlank()) return null;

      return item.createContent(Text.class)
          .withDescription("OCR from " + sourceContent.getId())
          .withData(textContent)
          .withProperties(sourceContent.getProperties())
          .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, sourceContent.getId())
          .save();
    }
  }

  /** Settings class for {@link OCR} */
  public static class Settings implements io.annot8.api.settings.Settings {
    // Processor Settings
    private List<String> extensions = Arrays.asList("bmp", "gif", "jpg", "jpeg", "tif", "tiff");

    // Tesseract Settings
    private List<String> configs = new ArrayList<>();
    private String dataPath = LoadLibs.extractTessResources("tessdata").toString();
    private String language = "eng";
    private int ocrEngine = TessAPI.TessOcrEngineMode.OEM_DEFAULT;
    private int pageSegmentation = -1;
    private Map<String, String> variables = new HashMap<>();

    @Description("List of file extensions (case insensitive) that will be OCR'd")
    public List<String> getExtensions() {
      return extensions;
    }

    public void setExtensions(List<String> extensions) {
      this.extensions = extensions;
    }

    @Description("List of Tesseract configs")
    public List<String> getConfigs() {
      return configs;
    }

    public void setConfigs(List<String> configs) {
      this.configs = configs;
    }

    @Description("Path to Tesseract models")
    public String getDataPath() {
      return dataPath;
    }

    public void setDataPath(String dataPath) {
      this.dataPath = dataPath;
    }

    @Description("Expected language of text")
    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    @Description("Tesseract engine to use")
    public int getOcrEngine() {
      return ocrEngine;
    }

    public void setOcrEngine(int ocrEngine) {
      this.ocrEngine = ocrEngine;
    }

    @Description("Tesseract page segmentation setting")
    public int getPageSegmentation() {
      return pageSegmentation;
    }

    public void setPageSegmentation(int pageSegmentation) {
      this.pageSegmentation = pageSegmentation;
    }

    @Description("Additional Tesseract variables")
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
