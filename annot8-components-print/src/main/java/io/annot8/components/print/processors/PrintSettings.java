/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.event.Level;

public class PrintSettings implements Settings {
  private boolean logOutput = true;
  private Level logLevel = Level.INFO;

  @Override
  public boolean validate() {
    return !logOutput || logLevel != null;
  }

  @Description(
      value =
          "If true, then the output will be sent to the configured logger rather than printed to the console",
      defaultValue = "true")
  public boolean isLogOutput() {
    return logOutput;
  }

  public void setLogOutput(boolean logOutput) {
    this.logOutput = logOutput;
  }

  @Description(
      value = "The level at which output should be logged - ignored if printing to the console",
      defaultValue = "INFO")
  public Level getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(Level logLevel) {
    this.logLevel = logLevel;
  }

  /**
   * Utility method for outputting a String in the correct way given the current settings
   *
   * @param logger Logger to output to (required if logOutput is true)
   * @param s The string to output
   */
  public void output(Logger logger, String s) {
    if (logOutput) {
      switch (logLevel) {
        case ERROR:
          logger.error(s);
          break;
        case WARN:
          logger.warn(s);
          break;
        case INFO:
          logger.info(s);
          break;
        case DEBUG:
          logger.debug(s);
          break;
        case TRACE:
          logger.trace(s);
          break;
      }
    } else {
      System.out.println(s);
    }
  }
}
