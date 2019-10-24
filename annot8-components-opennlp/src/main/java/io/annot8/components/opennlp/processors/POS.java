/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.common.data.utils.SortUtils;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

@ComponentName("OpenNLP Part of Speech")
@ComponentDescription("Annotate parts of speech identified by OpenNLP's POS detector")
@SettingsClass(POS.Settings.class)
public class POS extends AbstractProcessorDescriptor<POS.Processor, POS.Settings> {
  @Override
  protected Processor createComponent(Context context, Settings settings) {
    InputStream model;
    if (settings.getModel() == null) {
      model = POS.class.getResourceAsStream("en-pos-maxent.bin");
    } else {
      try {
        model = new FileInputStream(settings.getModel());
      } catch (IOException e) {
        throw new BadConfigurationException("Could not read POS model");
      }
    }

    return new Processor(model);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withProcessesAnnotations(
            AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN,
            SpanBounds
                .class) // Technically deletes and creates new ones, but equivalent to updating them
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private POSTaggerME detector;

    public Processor(InputStream model) {
      try {
        detector = new POSTaggerME(new POSModel(model));
      } catch (IOException ioe) {
        throw new BadConfigurationException("Cannot read POS model", ioe);
      }
    }

    @Override
    protected void process(Text content) {
      content
          .getAnnotations()
          .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
          .forEach(
              s -> {
                SpanBounds sentenceBounds = (SpanBounds) s.getBounds();
                // Get tokens for sentence
                List<Annotation> tokens = new ArrayList<>();
                content
                    .getBetween(sentenceBounds.getBegin(), sentenceBounds.getEnd())
                    .filter(a -> AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN.equals(a.getType()))
                    .filter(a -> a.getBounds() instanceof SpanBounds)
                    .sorted(SortUtils.SORT_BY_SPANBOUNDS)
                    .forEach(tokens::add);

                // Get POS for tokens
                String[] pos =
                    detector.tag(
                        tokens.stream()
                            .map(b -> b.getBounds(SpanBounds.class).get().getData(content).get())
                            .toArray(String[]::new));

                // Update each token
                for (int i = 0; i < pos.length; i++) {
                  Annotation original = tokens.get(i);

                  content
                      .getAnnotations()
                      .copy(original)
                      .withProperty(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, pos[i])
                      .save();
                }

                // Remove original annotation
                content.getAnnotations().delete(tokens);
              });
    }

    @Override
    public void close() {
      detector = null;
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private File model;

    @Override
    public boolean validate() {
      return true;
    }

    @Description("OpenNLP Part of Speech Model (or null to use default)")
    public File getModel() {
      return model;
    }

    public void setModel(File model) {
      this.model = model;
    }
  }
}
