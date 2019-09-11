package io.annot8.components.base.components;

import io.annot8.core.components.Processor;
import io.annot8.core.settings.Settings;

public abstract class AbstractProcessor<S extends Settings> extends AbstractComponent<S> implements Processor<S> {
}
