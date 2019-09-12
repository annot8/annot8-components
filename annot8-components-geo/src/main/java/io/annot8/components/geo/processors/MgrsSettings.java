/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

import io.annot8.core.settings.Settings;

public class MgrsSettings implements Settings {
  private final boolean ignoreDates;

  public MgrsSettings() {
    this.ignoreDates = false;
  }

  @JsonbCreator
  public MgrsSettings(@JsonbProperty("ignoreDates") boolean ignoreDates) {
    this.ignoreDates = ignoreDates;
  }

  public boolean isIgnoreDates() {
    return ignoreDates;
  }

  @Override
  public boolean validate() {
    return true;
  }
}
