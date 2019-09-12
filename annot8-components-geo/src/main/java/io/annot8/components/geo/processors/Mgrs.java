/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.opensextant.geodesy.MGRS;
import org.w3c.dom.Text;

import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.utils.java.StreamUtils;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.core.capabilities.AnnotationCapability;
import io.annot8.core.components.annotations.ComponentDescription;
import io.annot8.core.components.annotations.ComponentName;
import io.annot8.core.components.annotations.SettingsClass;
import io.annot8.core.settings.Settings;
import io.annot8.core.settings.SettingsClass;
import jdk.internal.loader.Resource;

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
