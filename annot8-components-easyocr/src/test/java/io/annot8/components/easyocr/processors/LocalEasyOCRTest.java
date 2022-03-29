/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.easyocr.processors;

import io.annot8.api.components.Processor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LocalEasyOCRTest {

  @Test
  @Disabled("Disabled as it requires EasyOCR to be installed, see README.md")
  public void integrationTest() throws Exception {
    LocalEasyOCR desc = new LocalEasyOCR();
    try (Processor ocr = desc.createComponent(null, new LocalEasyOCR.Settings())) {
      TestUtil.checkCanProcessFile(ocr);
      TestUtil.checkCanProcessImage(ocr);
    }
  }
}
