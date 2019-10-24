/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.annot8.components.opennlp.processors;

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
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ComponentName("OpenNLP Sentences")
@ComponentDescription("Annotate sentences identified by OpenNLP's sentence detector")
@SettingsClass(Sentences.Settings.class)
public class Sentences extends AbstractProcessorDescriptor<Sentences.Processor, Sentences.Settings> {
  @Override
  protected Processor createComponent(Context context, Settings settings) {
    InputStream model;
    if (settings.getModel() == null) {
      model = POS.class.getResourceAsStream("en-sent.bin");
    } else {
      try {
        model = new FileInputStream(settings.getModel());
      } catch (IOException e) {
        throw new BadConfigurationException("Could not read Sentence model");
      }
    }

    return new Processor(model);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private SentenceDetectorME detector;

    public Processor(InputStream model) {
      try {
        detector = new SentenceDetectorME(new SentenceModel(model));
      } catch (IOException ioe) {
        throw new BadConfigurationException("Cannot read Sentence model", ioe);
      }
    }

    @Override
    protected void process(Text content) {
      Span[] sentences = detector.sentPosDetect(lowerIfUpperCase(content.getData()));

      for (Span s : sentences) {
        content.getAnnotations().create()
            .withBounds(new SpanBounds(s.getStart(), s.getEnd()))
            .withType(AnnotationTypes.ANNOTATION_TYPE_SENTENCE)
            .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, s.getProb())
            .save();
      }
    }

    @Override
    public void close() {
      detector = null;
    }

    private String lowerIfUpperCase(String original){
      if(original.toUpperCase().equals(original))
        return original.toLowerCase();

      return original;
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private File model;

    @Override
    public boolean validate() {
      return true;
    }

    @Description("OpenNLP Sentence Model (or null to use default)")
    public File getModel() {
      return model;
    }
    public void setModel(File model) {
      this.model = model;
    }
  }
}
