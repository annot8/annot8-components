/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class AbstractCSVDataTest {

  protected File getTestData(String fileName) {
    URL url = AbstractCSVDataTest.class.getClassLoader().getResource(fileName);
    try {
      return new File(url.toURI());
    } catch (URISyntaxException e) {
      fail("Failed to find test resource", e);
    }
    return null;
  }
}
