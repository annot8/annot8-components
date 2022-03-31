/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

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
import io.annot8.common.data.content.Image;
import io.annot8.common.data.properties.EmptyImmutableProperties;
import io.annot8.conventions.PropertyKeys;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@ComponentName("Binarize Image")
@ComponentDescription("Binarize an image to black and white")
@ComponentTags({"image"})
@SettingsClass(BinarizeImage.Settings.class)
public class BinarizeImage
    extends AbstractProcessorDescriptor<BinarizeImage.Processor, BinarizeImage.Settings> {

  public enum Method {
    LUMINOSITY,
    OTSU
  }

  @Override
  protected Processor createComponent(Context context, BinarizeImage.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesContent(Image.class).build();
  }

  public static class Processor extends AbstractProcessor {

    private static final String PROPERTY_KEY_BINARY_METHOD = "binary-method";
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      if (item.getContents(Image.class).count() == 0L) return ProcessorResponse.ok();

      List<Exception> exceptions = new ArrayList<>();

      item.getContents(Image.class)
          .filter(c -> c.getProperties().get(PROPERTY_KEY_BINARY_METHOD).isEmpty())
          .forEach(
              c -> {
                Method method = settings.getMethod();
                log().info("Binarizing image {} using {}", c.getId(), method);
                try {
                  BufferedImage image = c.getData();
                  final int width = image.getWidth();
                  final int height = image.getHeight();
                  BufferedImage binaryImage;
                  switch (settings.getMethod()) {
                    case LUMINOSITY:
                      binaryImage = binarizeImageByLuminosity(image, width, height);
                      break;
                    case OTSU:
                      binaryImage = binarizeImageUsingOtsu(image, width, height);
                      break;
                    default:
                      throw new IllegalStateException("Unknown method: " + settings.getMethod());
                  }
                  addImage(item, c, binaryImage);
                } catch (Exception e) {
                  exceptions.add(e);
                  return;
                }

                if (settings.isDiscardOriginal()) item.removeContent(c);
              });

      if (exceptions.isEmpty()) {
        return ProcessorResponse.ok();
      } else {
        return ProcessorResponse.processingError(exceptions);
      }
    }

    private BufferedImage greyscale(BufferedImage image, int width, int height) {

      BufferedImage greyscale = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

      for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {

          Color color = new Color(image.getRGB(i, j));
          int alpha = color.getAlpha();
          int red = color.getRed();
          int green = color.getGreen();
          int blue = color.getBlue();

          // https://en.wikipedia.org/wiki/Grayscale
          int grey = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);

          new Color(grey, grey, grey, alpha).getRGB();

          greyscale.setRGB(i, j, new Color(grey, grey, grey, alpha).getRGB());
        }
      }
      return greyscale;
    }

    private BufferedImage binarizeImageByLuminosity(BufferedImage image, int width, int height) {

      BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

      for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {
          Color color = new Color(image.getRGB(i, j));
          int red = color.getRed();
          int green = color.getGreen();
          int blue = color.getBlue();
          int m = (red + green + blue);
          if (m >= 383) {
            binaryImage.setRGB(i, j, Color.WHITE.getRGB());
          } else {
            binaryImage.setRGB(i, j, Color.BLACK.getRGB());
          }
        }
      }
      return binaryImage;
    }

    private static int[] getHistogram(BufferedImage image, int width, int height) {
      final int[] histogram = new int[256];

      for (int i = 0; i < histogram.length; i++) histogram[i] = 0;

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          final int value = image.getRGB(x, y) & 0xFF;
          histogram[value]++;
        }
      }

      return histogram;
    }

    private static int getOtsuThreshold(int[] histogram, int noOfPixels) {

      int sum = 0;
      for (int i = 0; i < histogram.length; i++) {
        sum += i * histogram[i];
      }

      float sumB = 0;
      int wB = 0;
      int wF = 0;

      float varMax = 0;
      int threshold = 0;

      for (int i = 0; i < 256; i++) {
        wB += histogram[i];

        if (wB == 0) {
          continue;
        }

        wF = noOfPixels - wB;

        if (wF == 0) {
          break;
        }

        sumB += i * histogram[i];
        float mB = sumB / wB;
        float mF = (sum - sumB) / wF;

        float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

        if (varBetween > varMax) {
          varMax = varBetween;
          threshold = i;
        }
      }

      return threshold;
    }

    private BufferedImage binarizeImageUsingOtsu(BufferedImage image, int width, int height) {
      BufferedImage greyscale = greyscale(image, width, height);
      int[] histogram = getHistogram(greyscale, width, height);
      int threshold = getOtsuThreshold(histogram, width * height);

      BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

      for (int i = 0; i < width; i++) {
        for (int j = 0; j < height; j++) {
          final int value = greyscale.getRGB(i, j) & 0xFF;
          if (value > threshold) {
            binaryImage.setRGB(i, j, Color.WHITE.getRGB());
          } else {
            binaryImage.setRGB(i, j, Color.BLACK.getRGB());
          }
        }
      }
      return binaryImage;
    }

    private void addImage(Item item, Image original, BufferedImage image) {
      item.createContent(Image.class)
          .withId(original.getId() + "-binary")
          .withDescription(original.getDescription() + "-binary")
          .withData(image)
          .withProperties(
              settings.isCopyProperties()
                  ? original.getProperties()
                  : EmptyImmutableProperties.getInstance())
          .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, item.getId())
          .withProperty(PROPERTY_KEY_BINARY_METHOD, settings.getMethod())
          .withProperty(PropertyKeys.PROPERTY_KEY_WIDTH, image.getWidth())
          .withProperty(PropertyKeys.PROPERTY_KEY_HEIGHT, image.getHeight())
          .withProperty(PropertyKeys.PROPERTY_KEY_TYPE, image.getType())
          .save();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {

    private static final boolean DEFAULT_DISCARD = true;
    private static final boolean DEFAULT_COPY = true;
    private boolean discardOriginal = DEFAULT_DISCARD;
    private boolean copyProperties = DEFAULT_COPY;
    private Method method = Method.OTSU;

    @Override
    public boolean validate() {
      return method != null;
    }

    @Description("Set false to keep original item")
    public boolean isDiscardOriginal() {
      return discardOriginal;
    }

    public void setDiscardOriginal(boolean discardOriginal) {
      this.discardOriginal = discardOriginal;
    }

    public void setMethod(Method method) {
      this.method = method;
    }

    @Description("Set the scaling method to be used")
    public Method getMethod() {
      return method;
    }

    @Description("If true, then properties from the original image will be copied to the new image")
    public boolean isCopyProperties() {
      return copyProperties;
    }

    public void setCopyProperties(boolean copyProperties) {
      this.copyProperties = copyProperties;
    }

    public static class Builder {
      private boolean discardOriginal = DEFAULT_DISCARD;
      private Method method = Method.LUMINOSITY;
      private boolean copyProperties = DEFAULT_COPY;

      public Builder withDiscardOriginal(boolean discardOriginal) {
        this.discardOriginal = discardOriginal;
        return this;
      }

      public Builder withCopyProperties(boolean copyProperties) {
        this.copyProperties = copyProperties;
        return this;
      }

      public Builder withMethod(Method method) {
        this.method = method;
        return this;
      }

      public Settings build() {
        Settings settings = new Settings();
        settings.setDiscardOriginal(discardOriginal);
        settings.setMethod(method);
        settings.setCopyProperties(copyProperties);
        return settings;
      }
    }

    public static Builder builder() {
      return new Builder();
    }
  }
}
