package io.annot8.components.base.components;

import io.annot8.core.components.Processor;
import io.annot8.core.components.ProcessorDescriptor;
import io.annot8.core.settings.Settings;

public abstract class AbstractProcessorDescriptor<T extends Processor, S extends Settings> implements ProcessorDescriptor<T, S> {

  protected String name;
  protected S settings;

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
