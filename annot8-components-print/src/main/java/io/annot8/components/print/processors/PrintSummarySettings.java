/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import io.annot8.api.settings.Description;

public class PrintSummarySettings extends PrintSettings {

  private int reportProgress = 100;

  public PrintSettings setReportProgress(int reportProgress) {
    this.reportProgress = reportProgress;
    return this;
  }

  @Description("Report progress every 'n' items. Set to 0 to disable")
  public int getReportProgress() {
    return reportProgress;
  }
}
