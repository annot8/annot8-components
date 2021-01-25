/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opencv.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Image;
import io.annot8.components.opencv.utils.OpenCVUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRotatedRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

@ComponentName("Text Detection")
@ComponentDescription(
    "Detect within an image using the EAST algorithm, and extract text into separate images")
@SettingsClass(TextDetection.Settings.class)
public class TextDetection
    extends AbstractProcessorDescriptor<TextDetection.Processor, TextDetection.Settings> {
  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Image.class)
        .withCreatesContent(Image.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;
    private final Net eastNet;

    static {
      nu.pattern.OpenCV.loadLocally();
    }

    public Processor(Settings settings) {
      this.settings = settings;
      eastNet = Dnn.readNetFromTensorflow(settings.getEastModel().toString());
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Exception> exceptions = new ArrayList<>();

      // Snapshot the Image content, so we don't recursively end up processing images
      List<Image> images = item.getContents(Image.class).collect(Collectors.toList());

      images.forEach(
          img -> {
            // Based on code from: https://gist.github.com/berak/788da80d1dd5bade3f878210f45d6742
            Mat frame;
            try {
              frame = OpenCVUtils.bufferedImageToMat(img.getData());
            } catch (IOException ioe) {
              exceptions.add(ioe);
              return;
            }
            // Convert to 3-channel RGB
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

            // Calculate mean RGB values
            Scalar meanRGB = OpenCVUtils.meanRGB(img.getData());

            // Convert to blob
            Size size = new Size(settings.getSize(), settings.getSize());
            int height = (int) (size.height / 4);
            Mat blob = Dnn.blobFromImage(frame, 1.0, size, meanRGB, true, false);

            // Pass blob through to EAST and get outputs
            eastNet.setInput(blob);
            List<Mat> outs = new ArrayList<>(2);
            List<String> outNames = new ArrayList<>();
            outNames.add("feature_fusion/Conv_7/Sigmoid");
            outNames.add("feature_fusion/concat_3");
            eastNet.forward(outs, outNames);

            // Read results from EAST, and decode into RotatedRect
            Mat scores = outs.get(0).reshape(1, height);
            Mat geometry = outs.get(1).reshape(1, 5 * height);
            List<Float> confidencesList = new ArrayList<>();
            List<RotatedRect> boxesList =
                decode(scores, geometry, confidencesList, settings.getScoreThreshold());

            // Suppress non-maximal boxes
            MatOfFloat confidences =
                new MatOfFloat(Converters.vector_float_to_Mat(confidencesList));
            RotatedRect[] boxesArray = boxesList.toArray(new RotatedRect[0]);
            MatOfRotatedRect boxes = new MatOfRotatedRect(boxesArray);
            MatOfInt indices = new MatOfInt();
            Dnn.NMSBoxesRotated(
                boxes,
                confidences,
                settings.getScoreThreshold(),
                settings.getNmsThreshold(),
                indices);
            int[] indexes = indices.toArray();

            // Calculate the scaling ratio we need to apply
            Point ratio =
                new Point((float) frame.cols() / size.width, (float) frame.rows() / size.height);

            switch (settings.getOutputMode()) {
              case BOX:
                // Draw boxes around identified text
                for (int index : indexes) {
                  RotatedRect rot =
                      OpenCVUtils.padRotatedRect(boxesArray[index], settings.getPadding());
                  Point[] vertices = new Point[4];
                  rot.points(vertices);
                  for (int j = 0; j < 4; ++j) {
                    vertices[j].x *= ratio.x;
                    vertices[j].y *= ratio.y;
                  }
                  for (int j = 0; j < 4; ++j) {
                    Imgproc.line(
                        frame, vertices[j], vertices[(j + 1) % 4], new Scalar(0, 0, 255), 1);
                  }
                }

                try {
                  item.createContent(Image.class)
                      .withData(OpenCVUtils.matToBufferedImage(frame))
                      .withDescription("EAST output (BOX) from " + img.getId())
                      .save();
                } catch (IOException ioe) {
                  exceptions.add(ioe);
                  return;
                }
                break;
              case EXTRACT:
                // TODO: Merge detections that are adjacent
                // TODO: Rather than using bounding box, rotate rectangle
                for (int index : indexes) {
                  RotatedRect rot =
                      OpenCVUtils.padRotatedRect(boxesArray[index], settings.getPadding());
                  Rect bounding = rot.boundingRect();

                  item.createContent(Image.class)
                      .withData(
                          img.getData()
                              .getSubimage(
                                  (int) (bounding.x * ratio.x),
                                  (int) (bounding.y * ratio.y),
                                  (int) (bounding.width * ratio.x),
                                  (int) (bounding.height * ratio.y)))
                      .withDescription("EAST output (EXTRACT) from " + img.getId())
                      .save();

                  // TODO: Add properties to item - e.g. coordinates, size, original image, etc
                }

                break;
              case MASK:
                // Mask out non-text with black pixels
                Mat mask = new Mat(frame.rows(), frame.cols(), CvType.CV_8U);
                mask.setTo(OpenCVUtils.WHITE);
                for (int index : indexes) {
                  RotatedRect rot =
                      OpenCVUtils.padRotatedRect(boxesArray[index], settings.getPadding());
                  Point[] vertices = new Point[4];
                  rot.points(vertices);
                  for (int j = 0; j < 4; ++j) {
                    vertices[j].x *= ratio.x;
                    vertices[j].y *= ratio.y;
                  }

                  Imgproc.fillPoly(mask, List.of(new MatOfPoint(vertices)), OpenCVUtils.BLACK);
                }

                Imgproc.cvtColor(mask, mask, Imgproc.COLOR_GRAY2BGR, 3);
                frame.setTo(OpenCVUtils.BLACK, mask);

                try {
                  item.createContent(Image.class)
                      .withData(OpenCVUtils.matToBufferedImage(frame))
                      .withDescription("EAST output (MASK) from " + img.getId())
                      .save();
                } catch (IOException ioe) {
                  exceptions.add(ioe);
                  return;
                }

                break;
            }

            if (settings.isDiscardOriginal()) item.removeContent(img);
          });

      if (exceptions.isEmpty()) return ProcessorResponse.ok();

      return ProcessorResponse.processingError(exceptions);
    }

    private static List<RotatedRect> decode(
        Mat scores, Mat geometry, List<Float> confidences, float scoreThreshold) {
      int width = geometry.cols();
      int height = geometry.rows() / 5;

      List<RotatedRect> detections = new ArrayList<>();
      for (int y = 0; y < height; ++y) {
        Mat scoresData = scores.row(y);
        Mat x0Data = geometry.submat(0, height, 0, width).row(y);
        Mat x1Data = geometry.submat(height, 2 * height, 0, width).row(y);
        Mat x2Data = geometry.submat(2 * height, 3 * height, 0, width).row(y);
        Mat x3Data = geometry.submat(3 * height, 4 * height, 0, width).row(y);
        Mat anglesData = geometry.submat(4 * height, 5 * height, 0, width).row(y);

        for (int x = 0; x < width; ++x) {
          double score = scoresData.get(0, x)[0];
          if (score >= scoreThreshold) {
            double offsetX = x * 4.0;
            double offsetY = y * 4.0;
            double angle = anglesData.get(0, x)[0];
            double cosA = Math.cos(angle);
            double sinA = Math.sin(angle);
            double x0 = x0Data.get(0, x)[0];
            double x1 = x1Data.get(0, x)[0];
            double x2 = x2Data.get(0, x)[0];
            double x3 = x3Data.get(0, x)[0];
            double h = x0 + x2;
            double w = x1 + x3;
            Point offset =
                new Point(offsetX + cosA * x1 + sinA * x2, offsetY - sinA * x1 + cosA * x2);
            Point p1 = new Point(-1 * sinA * h + offset.x, -1 * cosA * h + offset.y);
            Point p3 = new Point(-1 * cosA * w + offset.x, sinA * w + offset.y);
            RotatedRect r =
                new RotatedRect(
                    new Point(0.5 * (p1.x + p3.x), 0.5 * (p1.y + p3.y)),
                    new Size(w, h),
                    -1 * angle * 180 / Math.PI);
            detections.add(r);
            confidences.add((float) score);
          }
        }
      }
      return detections;
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private boolean discardOriginal = false;
    private float scoreThreshold = 0.5f;
    private float nmsThreshold = 0.4f;
    private int size = 512;
    private Path eastModel;
    private OutputMode outputMode = OutputMode.MASK;
    private int padding = 0;

    @Override
    public boolean validate() {
      return true;
    }

    @Description("Should the original Content be discarded when an image is extracted?")
    public boolean isDiscardOriginal() {
      return discardOriginal;
    }

    public void setDiscardOriginal(boolean discardOriginal) {
      this.discardOriginal = discardOriginal;
    }

    @Description("Score threshold for the EAST algorithm")
    public float getScoreThreshold() {
      return scoreThreshold;
    }

    public void setScoreThreshold(float scoreThreshold) {
      this.scoreThreshold = scoreThreshold;
    }

    @Description("Non-Maximum Suppression (NMS) threshold for the EAST algorithm")
    public float getNmsThreshold() {
      return nmsThreshold;
    }

    public void setNmsThreshold(float nmsThreshold) {
      this.nmsThreshold = nmsThreshold;
    }

    @Description("Path to the EAST model")
    public Path getEastModel() {
      return eastModel;
    }

    public void setEastModel(Path eastModel) {
      this.eastModel = eastModel;
    }

    @Description("The size in pixels to scale images to for processing")
    public int getSize() {
      return size;
    }

    public void setSize(int size) {
      this.size = size;
    }

    @Description("How the results should be outputted")
    public OutputMode getOutputMode() {
      return outputMode;
    }

    public void setOutputMode(OutputMode outputMode) {
      this.outputMode = outputMode;
    }

    @Description("The amount of padding to add around detections, in scaled units")
    public int getPadding() {
      return padding;
    }

    public void setPadding(int padding) {
      this.padding = padding;
    }
  }

  public enum OutputMode {
    EXTRACT,
    MASK,
    BOX
  }
}
