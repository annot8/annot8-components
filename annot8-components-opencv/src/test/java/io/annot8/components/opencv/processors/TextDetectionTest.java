/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opencv.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.Image;
import io.annot8.testing.testimpl.TestItem;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

public class TextDetectionTest {
  @Test
  public void test() throws Exception {
    TextDetection.Settings s = new TextDetection.Settings();
    s.setEastModel(Path.of("/home/jdbaker/Downloads/frozen_east_text_detection.pb"));
    s.setDiscardOriginal(true);
    s.setOutputMode(TextDetection.OutputMode.MASK);
    s.setPadding(5);

    TextDetection.Processor p = new TextDetection.Processor(s);

    Item item = new TestItem();
    item.createContent(Image.class)
        .withData(ImageIO.read(new File("/home/jdbaker/Desktop/image.jpg")))
        .save();

    assertEquals(ProcessorResponse.ok(), p.process(item));
    System.out.println(item.getContents(Image.class).count());

    item.getContents(Image.class)
        .forEach(
            img -> {
              try {
                ImageIO.write(img.getData(), "png", new File(img.getId() + ".png"));
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
  }
}
