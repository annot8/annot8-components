/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.easyocr.processors;

public class InitSettings {

  private final boolean download;
  private final boolean gpu;
  private final String langs;

  public InitSettings(String langs, boolean download, boolean gpu) {
    this.langs = langs;
    this.download = download;
    this.gpu = gpu;
  }

  public boolean isDownload() {
    return download;
  }

  public boolean isGpu() {
    return gpu;
  }

  public String getLang() {
    return langs;
  }
}
