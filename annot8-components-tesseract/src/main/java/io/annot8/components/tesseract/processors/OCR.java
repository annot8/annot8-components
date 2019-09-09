/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.tesseract.processors;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import org.apache.commons.io.FilenameUtils;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.components.tesseract.processors.settings.TesseractSettings;
import io.annot8.core.capabilities.CreatesContent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.Settings;

/**
 * Takes FileContent containing either an image or PDF file, and produces a Text content with the
 * text from the file as extracted by Tesseract
 */
@ProcessesContent(FileContent.class)
@CreatesContent(Text.class)
public class OCR extends AbstractComponent implements Processor {
  private ITesseract instance = new Tesseract();
  private OCRSettings settings;

  @Override
  public void configure(Context context)
      throws BadConfigurationException, MissingResourceException {
    super.configure(context);
    this.settings = context.getSettings(OCRSettings.class).orElseGet(OCRSettings::new);

    if (!settings.validate()) throw new BadConfigurationException("Settings is not valid");

    settings.getTesseractSettings().configureInstance(instance);
  }

  @Override
  public ProcessorResponse process(Item item) {

    item.getContents(FileContent.class)
        .filter(
            fc ->
                settings
                    .getExtensions()
                    .contains(FilenameUtils.getExtension(fc.getData().getName()).toLowerCase()))
        .forEach(
            fc -> {
              try {
                String content = instance.doOCR(fc.getData());

                item.create(Text.class)
                    .withName(fc.getName() + settings.getSuffix())
                    .withData(content)
                    .withProperties(fc.getProperties())
                    .save();
              } catch (TesseractException e) {
                log().error("Unable to extract text from content {}", fc.getName(), e);
              }
            });

    return ProcessorResponse.ok();
  }

  /** Settings class for {@link OCR} */
  public static class OCRSettings extends AbstractComponent implements Settings {

    private final TesseractSettings tesseractSettings;
    private final List<String> extensions;
    private final String suffix;

    public static final List<String> DEFAULT_EXTENSIONS =
        Arrays.asList("bmp", "gif", "jpg", "jpeg", "pdf", "tif", "tiff");
    public static final String DEFAULT_SUFFIX = "_tesseract";

    public OCRSettings() {
      this.tesseractSettings = new TesseractSettings.Builder().withDefaultDataPath().build();
      this.extensions = DEFAULT_EXTENSIONS;
      this.suffix = DEFAULT_SUFFIX;
    }

    public OCRSettings(
        TesseractSettings tesseractSettings, List<String> extensions, String suffix) {
      this.tesseractSettings = tesseractSettings;
      this.extensions = extensions;
      this.suffix = suffix;
    }

    public TesseractSettings getTesseractSettings() {
      return tesseractSettings;
    }

    public List<String> getExtensions() {
      return extensions;
    }

    public String getSuffix() {
      return suffix;
    }

    @Override
    public boolean validate() {
      return tesseractSettings.validate()
          && extensions != null
          && !extensions.isEmpty()
          && suffix != null
          && !suffix.isEmpty();
    }
  }
}
