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
import io.annot8.api.properties.Properties;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.properties.EmptyImmutableProperties;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ComponentName("Flip, Rotate and Scale Image")
@ComponentDescription("Transform image content through flip, rotate and scale")
@ComponentTags({"image", "transform"})
@SettingsClass(FlipRotateScaleImage.Settings.class)
public class FlipRotateScaleImage
    extends AbstractProcessorDescriptor<
        FlipRotateScaleImage.Processor, FlipRotateScaleImage.Settings> {
  @Override
  protected Processor createComponent(Context context, FlipRotateScaleImage.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesContent(Image.class)
        .withDeletesContent(Image.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {

    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      Collection<Image> originalImages = item.getContents(Image.class).collect(Collectors.toList());

      originalImages.forEach(
          image -> {

            // TODO: Probably faster to do as many as possible of these in a single transform
            BufferedImage img =
                clipImage(
                    rotateImage(scaleImage(flipImage(image.getData()))),
                    image.getWidth(),
                    image.getHeight());

            Properties props;
            if (settings.isCopyProperties()) {
              props = image.getProperties();
            } else {
              props = EmptyImmutableProperties.getInstance();
            }

            Map<String, Object> transformDesc = new HashMap<>();
            transformDesc.put("clipImage", settings.isClipImage());
            transformDesc.put("flipHorizontal", settings.isFlipHorizontal());
            transformDesc.put("flipVertical", settings.isFlipVertical());
            transformDesc.put("rotation", settings.getRotate());
            transformDesc.put("scale", settings.getScale());

            item.createContent(Image.class)
                .withData(img)
                .withDescription("Transformed image from " + image.getId() + " - " + transformDesc)
                .withProperties(props)
                .withProperty("transform", transformDesc)
                .save();
          });

      if (settings.isDiscardOriginal()) {
        log().debug("Discarding {} original images", originalImages.size());
        originalImages.forEach(item::removeContent);
      }

      return ProcessorResponse.ok();
    }

    private BufferedImage flipImage(BufferedImage input) {
      if (!settings.isFlipHorizontal() && !settings.isFlipVertical()) return input;

      AffineTransform flipTransform = new AffineTransform();

      if (settings.isFlipHorizontal()) {
        flipTransform.scale(-1.0, 1.0);
        flipTransform.translate(-input.getWidth(), 0.0);
      }

      if (settings.isFlipVertical()) {
        flipTransform.scale(1.0, -1.0);
        flipTransform.translate(0.0, -input.getHeight());
      }

      AffineTransformOp ato = new AffineTransformOp(flipTransform, AffineTransformOp.TYPE_BILINEAR);
      return ato.filter(input, null);
    }

    private BufferedImage scaleImage(BufferedImage input) {
      if (settings.getScale() == 1.0 || settings.getScale() <= 0.0) return input;

      AffineTransform scaleTransform =
          AffineTransform.getScaleInstance(settings.getScale(), settings.getScale());
      AffineTransformOp ato =
          new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);
      return ato.filter(input, null);
    }

    private BufferedImage rotateImage(BufferedImage input) {
      if (settings.getRotate() % 360 == 0) return input;

      AffineTransform transform = new AffineTransform();

      double rad = Math.toRadians(settings.getRotate());
      double height =
          Math.abs(input.getWidth() * Math.sin(rad)) + Math.abs(input.getHeight() * Math.cos(rad));
      double width =
          Math.abs(input.getWidth() * Math.cos(rad)) + Math.abs(input.getHeight() * Math.sin(rad));

      transform.translate((width - input.getWidth()) / 2.0, (height - input.getHeight()) / 2.0);

      if (settings.getRotate() % 90.0 == 0) {
        transform.quadrantRotate(
            (int) (settings.getRotate() / 90), input.getWidth() / 2.0, input.getHeight() / 2.0);
      } else if (settings.getRotate() != 0.0) {
        transform.rotate(rad, input.getWidth() / 2.0, input.getHeight() / 2.0);
      }

      BufferedImage dest = new BufferedImage((int) width, (int) height, input.getType());

      AffineTransformOp ato = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
      ato.filter(input, dest);

      return dest;
    }

    private BufferedImage clipImage(BufferedImage input, int width, int height) {
      if (!settings.isClipImage()) return input;

      return input.getSubimage(
          (input.getWidth() - width) / 2, (input.getHeight() - height) / 2, width, height);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private boolean discardOriginal = false;
    private boolean flipVertical = false;
    private boolean flipHorizontal = false;
    private boolean clipImage = false;
    private boolean copyProperties = true;
    private double rotate = 0.0;
    private double scale = 1.0;

    @Override
    public boolean validate() {
      return scale > 0.0;
    }

    @Description("If true, then the original Image content will be discarded after transformation")
    public boolean isDiscardOriginal() {
      return discardOriginal;
    }

    public void setDiscardOriginal(boolean discardOriginal) {
      this.discardOriginal = discardOriginal;
    }

    @Description(
        "If true, then the image will be flipped vertically (i.e. about the horizontal axis) prior to rotation")
    public boolean isFlipVertical() {
      return flipVertical;
    }

    public void setFlipVertical(boolean flipVertical) {
      this.flipVertical = flipVertical;
    }

    @Description(
        "If true, then the image will be flipped vertically (i.e. about the vertical axis) prior to rotation")
    public boolean isFlipHorizontal() {
      return flipHorizontal;
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
      this.flipHorizontal = flipHorizontal;
    }

    @Description(
        "If true, then the image will be clipped to retain it's original size. If false, then the image will be expanded to cover the new bounds of the image.")
    public boolean isClipImage() {
      return clipImage;
    }

    public void setClipImage(boolean clipImage) {
      this.clipImage = clipImage;
    }

    @Description("If true, then properties from the original image will be copied to the new image")
    public boolean isCopyProperties() {
      return copyProperties;
    }

    public void setCopyProperties(boolean copyProperties) {
      this.copyProperties = copyProperties;
    }

    @Description(
        "The angle through which to rotate the image in degrees. Images are rotated about their centre.")
    public double getRotate() {
      return rotate;
    }

    public void setRotate(double rotate) {
      this.rotate = rotate;
    }

    @Description("The scaling factor that will be applied to the image")
    public double getScale() {
      return scale;
    }

    public void setScale(double scale) {
      this.scale = scale;
    }
  }
}
