/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;

/** Extract MGRS coordinates, optionally ignoring MGRS coordinates that could be dates */
@ComponentName("MGRS")
@ComponentDescription(
    "Extract MGRS coordinates, optionally ignoring MGRS coordinates that could be dates")
@SettingsClass(MgrsSettings.class)
public class Mgrs extends AbstractProcessorDescriptor<MgrsProcessor, MgrsSettings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_COORDINATE, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  public MgrsProcessor createComponent(Context context, MgrsSettings settings) {
    return new MgrsProcessor(settings);
  }
}
