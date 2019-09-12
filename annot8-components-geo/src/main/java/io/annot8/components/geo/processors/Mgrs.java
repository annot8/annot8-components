/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.components.base.components.AbstractProcessorDescriptor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.core.capabilities.AnnotationCapability;
import io.annot8.core.capabilities.ComponentCapabilities;
import io.annot8.core.components.annotations.ComponentDescription;
import io.annot8.core.components.annotations.ComponentName;
import io.annot8.core.components.annotations.SettingsClass;

import java.util.stream.Stream;

/** Extract MGRS coordinates, optionally ignoring MGRS coordinates that could be dates */
@ComponentName("MGRS")
@ComponentDescription("Extract MGRS coordinates, optionally ignoring MGRS coordinates that could be dates")
@SettingsClass(MgrsSettings.class)
public class Mgrs extends AbstractProcessorDescriptor<MgrsProcessor, MgrsSettings> {

  @Override
  public ComponentCapabilities capabilities() {
    return new ComponentCapabilities() {
      @Override
      public Stream<AnnotationCapability> createsAnnotations() {
        return Stream.of(new AnnotationCapability(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, SpanBounds.class));
      }
    };
  }

  @Override
  public MgrsProcessor create() {
    return new MgrsProcessor(settings);
  }
}
