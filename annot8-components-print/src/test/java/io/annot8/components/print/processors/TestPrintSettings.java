/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import org.slf4j.Logger;

public class TestPrintSettings extends PrintSummarySettings {

  StringBuilder builder = new StringBuilder();

  public void output(Logger logger, String s) {
    builder.append(s);
    builder.append("\n");
  }

  @Override
  public String toString() {
    return builder.toString();
  }
}
