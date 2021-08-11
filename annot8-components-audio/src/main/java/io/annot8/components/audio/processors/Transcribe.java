/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.audio.processors;

import com.google.gson.Gson;
import io.annot8.api.annotations.Annotation;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Audio;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupRoles;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioSystem;
import org.vosk.Model;
import org.vosk.Recognizer;

@ComponentName("Transcribe Audio to Text")
@ComponentDescription("Transcribe audio to text using the Vosk offline speech-to-text API")
@ComponentTags({"audio", "transcription"})
@SettingsClass(Transcribe.Settings.class)
public class Transcribe
    extends AbstractProcessorDescriptor<Transcribe.Processor, Transcribe.Settings> {
  @Override
  protected Processor createComponent(Context context, Transcribe.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(Audio.class)
            .withCreatesContent(Text.class)
            .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN, SpanBounds.class);

    if (getSettings().isAnnotateAudio())
      builder = builder.withCreatesGroups(GroupTypes.GROUP_TYPE_SAMEAS);

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    private final Model model;
    private final boolean annotateAudio;

    private static final Gson gson = new Gson();

    public Processor(Settings settings) {
      model = new Model(settings.getModel());
      annotateAudio = settings.isAnnotateAudio();
    }

    @Override
    public void close() {
      if (model != null) model.close();
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Exception> exceptions = new ArrayList<>();

      item.getContents(Audio.class)
          .forEach(
              audio -> {
                float asr = audio.getData().getFormat().getSampleRate();
                if (asr == AudioSystem.NOT_SPECIFIED) {
                  log()
                      .error(
                          "Sample rate of audio stream not specified - skipping {}", audio.getId());
                  return;
                }

                try (Recognizer recognizer = new Recognizer(model, asr)) {
                  recognizer.setWords(true);

                  int nbytes;
                  byte[] b = new byte[4096];
                  while ((nbytes = audio.getData().read(b)) >= 0) {
                    recognizer.acceptWaveForm(b, nbytes);
                  }

                  VoskOutput output = gson.fromJson(recognizer.getFinalResult(), VoskOutput.class);

                  Text t =
                      item.createContent(Text.class)
                          .withData(output.getText())
                          .withDescription("Transcribed audio from " + audio.getId())
                          .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, audio.getId())
                          .save();

                  int i = 0;
                  for (VoskOutputResult vor : output.getResult()) {
                    String next = output.getText().substring(i);

                    int start = i + next.indexOf(vor.getWord());
                    int end = start + vor.getWord().length();

                    Annotation aText =
                        t.getAnnotations()
                            .create()
                            .withBounds(new SpanBounds(start, end))
                            .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
                            .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, vor.getConf())
                            .save();

                    if (annotateAudio) {
                      int frameBegin = (int) Math.floor(vor.getStart() * asr);
                      int frameEnd = (int) Math.floor(vor.getEnd() * asr);

                      Annotation aAudio =
                          audio
                              .getAnnotations()
                              .create()
                              .withBounds(new SpanBounds(frameBegin, frameEnd))
                              .withType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN)
                              .withProperty(PropertyKeys.PROPERTY_KEY_PROBABILITY, vor.getConf())
                              .withProperty(PropertyKeys.PROPERTY_KEY_VALUE, vor.getWord())
                              .save();

                      item.getGroups()
                          .create()
                          .withType(GroupTypes.GROUP_TYPE_SAMEAS)
                          .withAnnotation(GroupRoles.GROUP_ROLE_MENTION, aText)
                          .withAnnotation(GroupRoles.GROUP_ROLE_MENTION, aAudio)
                          .save();
                    }

                    i = end;
                  }
                } catch (IOException e) {
                  log().error("Error reading Audio data", e);
                  exceptions.add(e);
                }
              });

      if (exceptions.isEmpty()) {
        return ProcessorResponse.ok();
      } else {
        return ProcessorResponse.itemError(exceptions);
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String model = null;
    private boolean annotateAudio = true;

    @Override
    public boolean validate() {
      return model != null;
    }

    @Description("Path to the Vosk model")
    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }

    @Description(
        "If true, then transcribed words will also be annotated in the Audio content and groups created to link word tokens in the text and in the audio")
    public boolean isAnnotateAudio() {
      return annotateAudio;
    }

    public void setAnnotateAudio(boolean annotateAudio) {
      this.annotateAudio = annotateAudio;
    }
  }

  private static class VoskOutput {
    private List<VoskOutputResult> result = Collections.emptyList();
    private String text = null;

    public List<VoskOutputResult> getResult() {
      return result;
    }

    public void setResult(List<VoskOutputResult> result) {
      this.result = result;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }
  }

  private static class VoskOutputResult {
    float conf = 0.0f;
    float start = 0.0f;
    float end = 0.0f;
    String word = "";

    public float getConf() {
      return conf;
    }

    public void setConf(float conf) {
      this.conf = conf;
    }

    public float getStart() {
      return start;
    }

    public void setStart(float start) {
      this.start = start;
    }

    public float getEnd() {
      return end;
    }

    public void setEnd(float end) {
      this.end = end;
    }

    public String getWord() {
      return word;
    }

    public void setWord(String word) {
      this.word = word;
    }
  }
}
