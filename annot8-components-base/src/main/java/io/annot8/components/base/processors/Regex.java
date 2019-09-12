package io.annot8.components.base.processors;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.components.base.components.AbstractProcessorDescriptor;
import io.annot8.core.capabilities.AnnotationCapability;
import io.annot8.core.capabilities.ComponentCapabilities;

import java.util.stream.Stream;

public class Regex extends AbstractProcessorDescriptor<RegexProcessor, RegexSettings> {
  @Override
  public ComponentCapabilities capabilities() {
    return new ComponentCapabilities() {
      @Override
      public Stream<AnnotationCapability> createsAnnotations() {
        return Stream.of(new AnnotationCapability(settings.getType(), SpanBounds.class));
      }
    };
  }

  @Override
  public RegexProcessor create() {
    return new RegexProcessor(settings);
  }
}
