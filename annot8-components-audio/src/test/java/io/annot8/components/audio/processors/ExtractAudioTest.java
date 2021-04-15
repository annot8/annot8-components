package io.annot8.components.audio.processors;

import io.annot8.api.data.Item;
import io.annot8.common.data.content.Audio;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.UriContent;
import io.annot8.conventions.PropertyKeys;
import io.annot8.testing.testimpl.TestItem;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class ExtractAudioTest {
  @Test
  public void testInputStream(){
    Item i = new TestItem();
    i.createContent(InputStreamContent.class)
      .withData(() -> ExtractAudioTest.class.getResourceAsStream("test.wav"))
      .save();

    ExtractAudio.Processor p = new ExtractAudio.Processor(new ExtractAudio.Settings());
    p.process(i);

    assertEquals(1, i.getContents(Audio.class).count());

    Audio a = i.getContents(Audio.class).findFirst().orElseThrow();
    assertNotNull(a.getDescription());
    assertTrue(a.getProperties().has(PropertyKeys.PROPERTY_KEY_PARENT));
  }

  @Test
  public void testUrl() throws URISyntaxException {
    URI uri = ExtractAudioTest.class.getResource("test.wav").toURI();

    Item i = new TestItem();
    i.createContent(UriContent.class)
      .withData(uri)
      .save();

    ExtractAudio.Processor p = new ExtractAudio.Processor(new ExtractAudio.Settings());
    p.process(i);

    assertEquals(1, i.getContents(Audio.class).count());

    Audio a = i.getContents(Audio.class).findFirst().orElseThrow();
    assertNotNull(a.getDescription());
    assertTrue(a.getProperties().has(PropertyKeys.PROPERTY_KEY_PARENT));
  }
}
