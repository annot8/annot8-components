/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opencv.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

public class OpenCVUtils {
  private OpenCVUtils() {
    // Private constructor for utility class
  }

  public static final Scalar BLACK = new Scalar(0, 0, 0);
  public static final Scalar WHITE = new Scalar(255, 255, 255);

  public static RotatedRect padRotatedRect(RotatedRect orig, int padding) {
    return new RotatedRect(
        orig.center,
        new Size(orig.size.width + 2 * padding, orig.size.height + 2 * padding),
        orig.angle);
  }

  public static Point[] scaleRotatedRect(RotatedRect rect, double xScale, double yScale) {
    Point[] vertices = new org.opencv.core.Point[4];
    rect.points(vertices);

    for (int j = 0; j < 4; ++j) {
      vertices[j].x *= xScale;
      vertices[j].y *= yScale;
    }

    return vertices;
  }

  public static Mat bufferedImageToMat(BufferedImage image) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", byteArrayOutputStream);
    byteArrayOutputStream.flush();
    return Imgcodecs.imdecode(
        new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
  }

  public static BufferedImage matToBufferedImage(Mat matrix) throws IOException {
    MatOfByte mob = new MatOfByte();
    Imgcodecs.imencode(".png", matrix, mob);
    return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
  }

  public static Scalar meanRGB(BufferedImage img) {
    double meanRed = 0.0;
    double meanGreen = 0.0;
    double meanBlue = 0.0;

    for (int y = 0; y < img.getHeight(); ++y) {
      for (int x = 0; x < img.getWidth(); ++x) {
        Color r = new Color(img.getRGB(x, y));
        meanRed += r.getRed();
        meanGreen += r.getGreen();
        meanBlue += r.getBlue();
      }
    }

    meanRed = meanRed / (img.getWidth() * img.getHeight());
    meanGreen = meanGreen / (img.getWidth() * img.getHeight());
    meanBlue = meanBlue / (img.getWidth() * img.getHeight());

    return new Scalar(meanRed, meanGreen, meanBlue);
  }
}
