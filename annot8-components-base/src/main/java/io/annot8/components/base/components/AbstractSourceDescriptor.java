package io.annot8.components.base.components;

import io.annot8.core.components.Source;
import io.annot8.core.components.SourceDescriptor;
import io.annot8.core.settings.Settings;

public abstract class AbstractSourceDescriptor<T extends Source, S extends Settings> implements SourceDescriptor<T, S> {

  private String name;
  private S settings;

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setSettings(S settings) {
    this.settings = settings;
  }

  @Override
  public S getSettings() {
    return settings;
  }
}
