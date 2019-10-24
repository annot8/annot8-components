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

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
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
import io.annot8.conventions.GroupRoles;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.util.Span;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ComponentName("OpenNLP Phrase Chunks")
@ComponentDescription("Annotate phrase chunks identified by OpenNLP's chunker")
@SettingsClass(PhraseChunks.Settings.class)
public class PhraseChunks extends AbstractProcessorDescriptor<PhraseChunks.Processor, PhraseChunks.Settings> {
  @Override
  protected Processor createComponent(Context context, Settings settings) {
    InputStream model;
    if (settings.getModel() == null) {
      model = POS.class.getResourceAsStream("en-chunker.bin");
    } else {
      try {
        model = new FileInputStream(settings.getModel());
      } catch (IOException e) {
        throw new BadConfigurationException("Could not read Chunker model");
      }
    }

    return new Processor(model);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_SENTENCE, SpanBounds.class)
        .withProcessesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class)
        .withCreatesGroups(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
        .build();
  }

  public static class Processor extends AbstractTextProcessor{
    private ChunkerME phraseChunker;
    private final Set<String> prepositions = Set.of(
            "about",
            "above",
            "across",
            "against",
            "amid",
            "around",
            "at",
            "atop",
            "behind",
            "below",
            "beneath",
            "beside",
            "between",
            "beyond",
            "by",
            "for",
            "from",
            "down",
            "in",
            "including",
            "inside",
            "into",
            "mid",
            "near",
            "of",
            "off",
            "on",
            "onto",
            "opposite",
            "out",
            "outside",
            "over",
            "round",
            "through",
            "throughout",
            "to",
            "under",
            "underneath",
            "with",
            "within",
            "without");

    public Processor(InputStream model){
      try {
        phraseChunker = new ChunkerME(new ChunkerModel(model));
      } catch (IOException ioe) {
        throw new BadConfigurationException("Cannot read Chunker model", ioe);
      }
    }

    @Override
    protected void process(Text content) {
      content.getAnnotations().getByBoundsAndType(SpanBounds.class, AnnotationTypes.ANNOTATION_TYPE_SENTENCE).forEach(s -> {
        SpanBounds sentenceBounds = (SpanBounds) s.getBounds();
        //Get tokens for sentence
        List<Annotation> tokens = new ArrayList<>();
        content.getBetween(sentenceBounds.getBegin(), sentenceBounds.getEnd())
            .filter(a -> AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN.equals(a.getType()))
            .filter(a -> a.getBounds() instanceof SpanBounds)
            .sorted(SortUtils.SORT_BY_SPANBOUNDS)
            .forEach(tokens::add);

        String[] words = new String[tokens.size()];
        String[] pos = new String[tokens.size()];

        int i = 0;
        for(Annotation a : tokens){
          String word = content.getText(a).orElse("");
          String tag = a.getProperties().get(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, String.class).orElse("UNK");

          words[i] = word;
          pos[i] = tag;
          i++;
        }

        Span[] spans = phraseChunker.chunkAsSpans(words, pos);

        for (Span span : spans){
          List<Annotation> constituentWords = content
              .getBetween(tokens.get(span.getStart()).getBounds(SpanBounds.class).get().getBegin(), tokens.get(span.getEnd()).getBounds(SpanBounds.class).get().getEnd())
              .filter(a -> AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN.equals(a.getType()))
              .collect(Collectors.toList());

          int headWordId = constituentWords.size() - 1;

          // Run through prior words, check for propositional - if so skip, if not break
          for (int a = constituentWords.size() - 2; a > 1; a--) {
            String cwPos = constituentWords.get(a).getProperties()
                .get(PropertyKeys.PROPERTY_KEY_PARTOFSPEECH, String.class).orElse("UNK");
            String cwText = content.getText(constituentWords.get(a)).orElse("");

            // If a POS tag or word value is prepositional, end increment head word index
            if ("IN".equals(cwPos)
                || ",".equals(cwPos)
                || prepositions.contains(cwText)) {
              headWordId = a - 1;
            } else {
              headWordId = a;
              break;
            }
          }

          Group.Builder builder = content.getItem().getGroups().create()
              .withType(GroupTypes.GROUP_TYPE_GRAMMAR_PHRASE)
              .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, span.getProb())
              .withProperty(PropertyKeys.PROPERTY_KEY_SUBTYPE, span.getType());

          for (int a = 0; a < constituentWords.size(); a++) {
            if(a == headWordId){
              builder = builder.withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_HEAD, constituentWords.get(a));
            }else {
              builder = builder.withAnnotation(GroupRoles.GROUP_ROLE_GRAMMAR_CONSTITUENT, constituentWords.get(a));
            }
          }

          builder.save();
        }
      });
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private File model;

    @Override
    public boolean validate() {
      return true;
    }

    @Description("OpenNLP Phrase Chunk Model (or null to use default)")
    public File getModel() {
      return model;
    }
    public void setModel(File model) {
      this.model = model;
    }
  }
}
