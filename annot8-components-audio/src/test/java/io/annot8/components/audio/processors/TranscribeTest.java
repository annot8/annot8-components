/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.audio.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.data.Item;
import io.annot8.common.data.content.Audio;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.io.BufferedInputStream;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.api.Test;

public class TranscribeTest {
  @Test
  public void test() throws Exception {
    Transcribe.Settings s = new Transcribe.Settings();
    s.setAnnotateAudio(true);
    s.setModel(
        "src/test/resources/io/annot8/components/audio/processors/vosk-model-small-en-us-0.15/");

    Item i = new TestItem();
    Audio a =
        i.createContent(Audio.class)
            .withData(
                AudioSystem.getAudioInputStream(
                    new BufferedInputStream(TranscribeTest.class.getResourceAsStream("test.wav"))))
            .save();

    Transcribe.Processor p = new Transcribe.Processor(s);
    p.process(i);

    assertEquals(1, i.getContents(Text.class).count());
    Text t = i.getContents(Text.class).findFirst().orElseThrow();

    assertTrue(t.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).count() > 0);
    assertTrue(a.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).count() > 0);
    assertTrue(i.getGroups().getAll().count() > 0);

    p.close();
  }

  @Test
  public void testNoAudioAnnotation() throws Exception {
    Transcribe.Settings s = new Transcribe.Settings();
    s.setAnnotateAudio(false);
    s.setModel(
        "src/test/resources/io/annot8/components/audio/processors/vosk-model-small-en-us-0.15/");

    Item i = new TestItem();
    Audio a =
        i.createContent(Audio.class)
            .withData(
                AudioSystem.getAudioInputStream(
                    new BufferedInputStream(TranscribeTest.class.getResourceAsStream("test.wav"))))
            .save();

    Transcribe.Processor p = new Transcribe.Processor(s);
    p.process(i);

    assertEquals(1, i.getContents(Text.class).count());
    Text t = i.getContents(Text.class).findFirst().orElseThrow();

    assertTrue(t.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).count() > 0);
    assertEquals(
        0, a.getAnnotations().getByType(AnnotationTypes.ANNOTATION_TYPE_WORDTOKEN).count());
    assertEquals(0, i.getGroups().getAll().count());

    p.close();
  }

  // TODO: Test settings
  @Test
  public void testDescriptor() {
    Transcribe.Settings s = new Transcribe.Settings();
    s.setAnnotateAudio(false);
    s.setModel(
        "src/test/resources/io/annot8/components/audio/processors/vosk-model-small-en-us-0.15/");

    Transcribe t = new Transcribe();
    t.setSettings(s);

    assertNotNull(t.capabilities());
    try (Transcribe.Processor p = t.createComponent(new SimpleContext(), s)) {
      assertNotNull(p);
    }
  }
}
