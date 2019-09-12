package io.annot8.components.geo.processors;

import io.annot8.core.settings.Settings;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

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
