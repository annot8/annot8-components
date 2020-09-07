/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.source;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.responses.SourceResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.ItemFactory;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractSource;
import io.annot8.common.components.AbstractSourceDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;

/**
 * A Source which will always return done, without creating any items.
 *
 * <p>Use this in cases were you need a pipeline, but do not need a source.
 */
@ComponentName("Empty Source")
@ComponentDescription("A Source which will always return done, without creating any items")
public class EmptySource extends AbstractSourceDescriptor<EmptySource.Source, NoSettings> {
  @Override
  protected Source createComponent(Context context, NoSettings settings) {
    return new Source();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().build();
  }

  public static class Source extends AbstractSource {
    @Override
    public SourceResponse read(ItemFactory itemFactory) {
      return SourceResponse.done();
    }
  }
}
