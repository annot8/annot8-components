/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.opennlp.processors;

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
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

@ComponentName("OpenNLP Named Entity Recognition")
@ComponentDescription(
    "Use OpenNLP Named Entity Recognition (NER) models to extract named entities as annotations")
@SettingsClass(NER.Settings.class)
public class NER extends AbstractProcessorDescriptor<NER.Processor, NER.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.getModel(), settings.getType());
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withCreatesAnnotations(getSettings().getType(), SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private NameFinderME nameFinder;
    private String type;

    public Processor(File model, String type) {
      this.type = type;
      try {
        nameFinder = new NameFinderME(new TokenNameFinderModel(model));
      } catch (IOException ioe) {
        throw new BadConfigurationException("Cannot read NER model", ioe);
      }
    }

    public Processor(InputStream model, String type) {
      this.type = type;
      try {
        nameFinder = new NameFinderME(new TokenNameFinderModel(model));
      } catch (IOException ioe) {
        throw new BadConfigurationException("Cannot read NER model", ioe);
      }
    }

    @Override
    protected void process(Text content) {
      Stream<Annotation> sentences =
          content
              .getAnnotations()
              .getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_SENTENCE);

      sentences.forEach(
          s -> {
            SpanBounds bounds = (SpanBounds) s.getBounds();

            List<SpanBounds> tokens = new ArrayList<>();
            content
                .getBetween(bounds.getBegin(), bounds.getEnd())
                .filter(a -> AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN.equals(a.getType()))
                .filter(a -> a.getBounds() instanceof SpanBounds)
                .sorted(Comparator.comparingInt(a -> ((SpanBounds) a.getBounds()).getBegin()))
                .forEach(a -> tokens.add((SpanBounds) a.getBounds()));

            Span[] spans =
                nameFinder.find(
                    tokens.stream().map(b -> b.getData(content).get()).toArray(String[]::new));

            for (Span span : spans) {
              int begin = tokens.get(span.getStart()).getBegin();
              int end = tokens.get(span.getEnd() - 1).getEnd();

              content
                  .getAnnotations()
                  .create()
                  .withBounds(new SpanBounds(begin, end))
                  .withType(type)
                  .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, span.getProb())
                  .save();
            }
          });

      nameFinder.clearAdaptiveData();
    }

    @Override
    public void close() {
      nameFinder = null;
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private File model;
    private String type;

    @Override
    public boolean validate() {
      return model != null;
    }

    @Description("OpenNLP NER Model File")
    public File getModel() {
      return model;
    }

    public void setModel(File model) {
      this.model = model;
    }

    @Description("Entity type")
    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
}
