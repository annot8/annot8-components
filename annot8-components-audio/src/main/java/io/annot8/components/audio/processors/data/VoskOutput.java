/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.audio.processors.data;

import java.util.Collections;
import java.util.List;

public class VoskOutput {
  private List<VoskOutputResult> result = Collections.emptyList();
  private String text = null;

  public List<VoskOutputResult> getResult() {
    return result;
  }

  public void setResult(List<VoskOutputResult> result) {
    this.result = result;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}
