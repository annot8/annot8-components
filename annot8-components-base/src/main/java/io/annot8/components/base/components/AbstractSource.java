package io.annot8.components.base.components;

import io.annot8.core.components.Source;
import io.annot8.core.settings.Settings;

public abstract class AbstractSource<S extends Settings> extends AbstractComponent<S> implements Source<S> {
}
