/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.annot8.common.data.content.Image;
import io.annot8.testing.testimpl.TestItem;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

public class SaveImageTest {
  @Test
  public void testJpg() throws IOException {
    test(SaveImage.FileType.JPG);
  }

  @Test
  public void testPng() throws IOException {
    test(SaveImage.FileType.PNG);
  }

  @Test
  public void testSettings() {
    SaveImage.Settings settings = new SaveImage.Settings();
    assertTrue(settings.validate());

    settings.setFileType(SaveImage.FileType.JPG);
    assertEquals(SaveImage.FileType.JPG, settings.getFileType());
    assertTrue(settings.validate());

    settings.setFileType(SaveImage.FileType.PNG);
    assertEquals(SaveImage.FileType.PNG, settings.getFileType());
    assertTrue(settings.validate());

    settings.setOutputFolder(Path.of("foo"));
    assertEquals(Path.of("foo"), settings.getOutputFolder());
    assertTrue(settings.validate());

    settings.setOutputFolder(null);
    assertFalse(settings.validate());

    settings.setOutputFolder(Path.of("."));
    assertTrue(settings.validate());

    settings.setFileType(null);
    assertFalse(settings.validate());
  }

  @Test
  public void testDescriptor() {
    SaveImage saveImage = new SaveImage();
    assertNotNull(saveImage.capabilities());
    assertNotNull(saveImage.createComponent(null, new SaveImage.Settings()));
  }

  private void test(SaveImage.FileType fileType) throws IOException {
    Path tempFolder = Files.createTempDirectory("saveimagetest-");

    SaveImage.Settings settings = new SaveImage.Settings();
    settings.setFileType(fileType);
    settings.setOutputFolder(tempFolder);

    try (SaveImage.Processor processor = new SaveImage.Processor(settings)) {

      BufferedImage img;
      try (InputStream is =
          SaveImageTest.class.getClassLoader().getResourceAsStream("testimage.jpg")) {
        img = ImageIO.read(is);
      }

      TestItem item = new TestItem();
      Image content1 = item.createContent(Image.class).withData(img).save();
      Image content2 = item.createContent(Image.class).withData(img).save();

      processor.process(item);

      String extension;
      if (fileType == SaveImage.FileType.PNG) {
        extension = ".png";
      } else {
        extension = ".jpg";
      }

      Path expected1 = tempFolder.resolve(item.getId()).resolve(content1.getId() + extension);
      Path expected2 = tempFolder.resolve(item.getId()).resolve(content2.getId() + extension);

      assertTrue(expected1.toFile().exists());
      assertTrue(Files.size(expected1) > 0L);

      assertTrue(expected2.toFile().exists());
      assertTrue(Files.size(expected2) > 0L);

      expected1.toFile().delete();
      expected2.toFile().delete();
      tempFolder.resolve(item.getId()).toFile().delete();
      tempFolder.toFile().delete();
    }
  }
}
