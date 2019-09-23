package io.annot8.components.base.source;

import io.annot8.api.capabilities.Capabilities;
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
 * Use this in cases were you need a pipeline, but do not need a source.
 *
 */
public class EmptySource extends AbstractSource {

  @Override
  public SourceResponse read(ItemFactory itemFactory) {
    return SourceResponse.done();
  }

  /**
   * A descriptor for EmptySource.
   *
   * It is unlikely the EmptySource be of use in pipelines.
   */
  public static class Descriptor extends AbstractSourceDescriptor<EmptySource, NoSettings> {

    @Override
    protected EmptySource createComponent(Context context, NoSettings settings) {
      return new EmptySource();
    }

    @Override
    public Capabilities capabilities() {
      return new SimpleCapabilities.Builder().build();
    }
  }

}
