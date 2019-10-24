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
import io.annot8.common.data.bounds.ContentBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ComponentName("OpenNLP Language Detection")
@ComponentDescription("Annotate tokens identified by OpenNLP's language detector")
@SettingsClass(LanguageDetection.Settings.class)
public class LanguageDetection extends AbstractProcessorDescriptor<LanguageDetection.Processor, LanguageDetection.Settings> {
  @Override
  protected Processor createComponent(Context context, Settings settings) {
    InputStream model;
    if (settings.getModel() == null) {
      model = Tokens.class.getResourceAsStream("langdetect-183.bin");
    } else {
      try {
        model = new FileInputStream(settings.getModel());
      } catch (IOException e) {
        throw new BadConfigurationException("Could not read Language Detection model");
      }
    }

    return new Processor(model);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_LANGUAGE, ContentBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private LanguageDetector detector;

    public Processor(InputStream model) {
      try {
        detector = new LanguageDetectorME(new LanguageDetectorModel(model));
      } catch (IOException ioe) {
        throw new BadConfigurationException("Cannot read Language Detection model", ioe);
      }
    }

    @Override
    protected void process(Text content) {
      Language l = detector.predictLanguage(content.getData());

      content.getAnnotations().create()
          .withType(AnnotationTypes.ANNOTATION_TYPE_LANGUAGE)
          .withBounds(ContentBounds.getInstance())
          .withProperty(PropertyKeys.PROPERTY_KEY_LANGUAGE, l.getLang())
          .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, l.getConfidence())
          .save();
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

    @Description("OpenNLP Language Detection Model (or null to use default)")
    public File getModel() {
      return model;
    }
    public void setModel(File model) {
      this.model = model;
    }
  }
}