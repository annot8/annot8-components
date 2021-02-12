/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.conventions.PropertyKeys;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.poifs.filesystem.FileMagic;

@ComponentName("PDF Renderer")
@ComponentDescription("Renders a PDF (*.pdf) file to an Image, with one image per page")
@ComponentTags({"documents", "pdf", "render", "images"})
@SettingsClass(RenderPdf.Settings.class)
public class RenderPdf
    extends AbstractProcessorDescriptor<RenderPdf.Processor, RenderPdf.Settings> {
  @Override
  protected Processor createComponent(Context context, RenderPdf.Settings settings) {
    return new Processor(settings.getDpi());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(InputStreamContent.class)
        .withProcessesContent(FileContent.class)
        .withCreatesContent(Image.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    private final int dpi;

    public Processor(int dpi) {
      this.dpi = dpi;
    }

    @Override
    public ProcessorResponse process(Item item) {
      item.getContents(FileContent.class)
          .filter(c -> c.getData().getName().toLowerCase().endsWith(".pdf"))
          .forEach(
              c -> {
                PDDocument document;
                try {
                  document = PDDocument.load(c.getData());
                } catch (IOException ioe) {
                  log().error("Unable to read PDF file for FileContent {}", c.getId(), ioe);
                  return;
                }

                if (document != null) {
                  renderPdf(item, c.getId(), document);

                  try {
                    document.close();
                  } catch (IOException e) {
                    // Do nothing, we're closing anyway
                  }
                }
              });

      item.getContents(InputStreamContent.class)
          .filter(
              c -> {
                try (InputStream is = c.getData()) {
                  return FileMagic.valueOf(new BufferedInputStream(is)) == FileMagic.PDF;
                } catch (IOException ioe) {
                  return false;
                }
              })
          .forEach(
              c -> {
                PDDocument document;
                try {
                  document = PDDocument.load(c.getData());
                } catch (IOException ioe) {
                  log().error("Unable to read PDF file for FileContent {}", c.getId(), ioe);
                  return;
                }

                if (document != null) {
                  renderPdf(item, c.getId(), document);

                  try {
                    document.close();
                  } catch (IOException e) {
                    // Do nothing, we're closing anyway
                  }
                }
              });

      return ProcessorResponse.ok();
    }

    private void renderPdf(Item item, String parentId, PDDocument document) {
      PDFRenderer pdfRenderer = new PDFRenderer(document);
      for (int page = 0; page < document.getNumberOfPages(); ++page) {
        BufferedImage bImg;
        try {
          bImg = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
        } catch (IOException ioe) {
          log().error("Unable to render page {} from {}", page, parentId, ioe);
          continue;
        }

        item.createContent(Image.class)
            .withData(bImg)
            .withDescription(
                "Rendered page "
                    + page
                    + " of "
                    + document.getNumberOfPages()
                    + " from "
                    + parentId)
            .withProperty(PropertyKeys.PROPERTY_KEY_PAGE, page)
            .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, parentId)
            .save();
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private int dpi = 300;

    @Override
    public boolean validate() {
      return dpi > 0;
    }

    @Description("The DPI to render the PDFs at")
    public int getDpi() {
      return dpi;
    }

    public void setDpi(int dpi) {
      this.dpi = dpi;
    }
  }
}
