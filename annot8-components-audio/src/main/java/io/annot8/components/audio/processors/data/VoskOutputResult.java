/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.audio.processors.data;

public class VoskOutputResult {
  private float conf = 0.0f;
  private float start = 0.0f;
  private float end = 0.0f;
  private String word = "";

  public float getConf() {
    return conf;
  }

  public void setConf(float conf) {
    this.conf = conf;
  }

  public float getStart() {
    return start;
  }

  public void setStart(float start) {
    this.start = start;
  }

  public float getEnd() {
    return end;
  }

  public void setEnd(float end) {
    this.end = end;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }
}
