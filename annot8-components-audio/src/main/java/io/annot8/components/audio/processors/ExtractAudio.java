package io.annot8.components.audio.processors;


import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.Audio;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.UriContent;
import io.annot8.conventions.PropertyKeys;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ComponentName("Extract Audio")
@ComponentDescription("Extract audio content from Files for processing")
@ComponentTags({"audio"})
@SettingsClass(ExtractAudio.Settings.class)
public class ExtractAudio extends AbstractProcessorDescriptor<ExtractAudio.Processor, ExtractAudio.Settings> {
  @Override
  protected Processor createComponent(Context context, ExtractAudio.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
      .withProcessesContent(FileContent.class)
      .withProcessesContent(InputStreamContent.class)
      .withProcessesContent(UriContent.class)
      .withCreatesContent(Audio.class)
      .build();
  }

  public static class Processor extends AbstractProcessor {
    private final List<String> extensions;
    private final boolean discardOriginal;

    public Processor(Settings settings) {
      extensions =
        settings.getFileExtensions().stream()
          .map(s -> s.trim().toLowerCase())
          .collect(Collectors.toList());
      discardOriginal = settings.isDiscardOriginal();
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Exception> exceptions = new ArrayList<>();
      // Process InputStreamContent
      item.getContents(InputStreamContent.class)
        .forEach(
          isc -> {
            final AudioInputStream ais;
            final AudioFileFormat aff;

            try {
              if(isc.getData().markSupported()) {
                ais = AudioSystem.getAudioInputStream(isc.getData());
                aff = AudioSystem.getAudioFileFormat(isc.getData());
              }else{
                log().debug("Wrapping InputStream {} in BufferedInputStream", isc.getId());

                ais = AudioSystem.getAudioInputStream(new BufferedInputStream(isc.getData()));
                aff = AudioSystem.getAudioFileFormat(new BufferedInputStream(isc.getData()));
              }

            } catch (UnsupportedAudioFileException e) {
              log().warn("Unsupported audio format for InputStreamContent {}", isc.getId(), e);
              return;
            } catch (IOException e) {
              log().error("Error reading InputStreamContent {}", isc.getId(), e);
              exceptions.add(e);
              return;
            }

            if (discardOriginal) item.removeContent(isc);

            createContent(item, ais, aff, "Audio extracted from InputStreamContent "+ isc.getId(), isc.getId());
          });

      // Process FileContent
      item.getContents(FileContent.class)
        .filter(
          fc ->
            extensions.stream()
              .anyMatch(ext -> fc.getData().getName().toLowerCase().endsWith("." + ext)))
        .forEach(
          fc -> {
            final AudioInputStream ais;
            final AudioFileFormat aff;

            try {
              ais = AudioSystem.getAudioInputStream(fc.getData());
              aff = AudioSystem.getAudioFileFormat(fc.getData());

            } catch (UnsupportedAudioFileException e) {
              log().warn("Unsupported audio format for File {}", fc.getData().getPath(), e);
              return;
            } catch (IOException e) {
              log().error("Error reading File {}", fc.getData().getPath(), e);
              exceptions.add(e);
              return;
            }

            if (discardOriginal) item.removeContent(fc);

            createContent(item, ais, aff, "Audio extracted from File "+ fc.getData().getPath(), fc.getId());
          });

      // Process UriContent
      item.getContents(UriContent.class)
        .forEach(
          uc -> {
            final AudioInputStream ais;
            final AudioFileFormat aff;
            try {
              ais = AudioSystem.getAudioInputStream(uc.getData().toURL());
              aff = AudioSystem.getAudioFileFormat(uc.getData().toURL());
            } catch (UnsupportedAudioFileException e) {
              log().warn("Unsupported audio format for URI {}", uc.getData(), e);
              return;
            } catch (IOException e) {
              log().error("Error reading URI {}", uc.getData(), e);
              exceptions.add(e);
              return;
            }

            if (discardOriginal) item.removeContent(uc);

            createContent(item, ais, aff, "Audio extracted from URI "+ uc.getData(), uc.getId());
          });

      if (exceptions.isEmpty()) {
        return ProcessorResponse.ok();
      } else {
        return ProcessorResponse.processingError(exceptions);
      }
    }

    private void createContent(Item item, AudioInputStream audioInputStream, AudioFileFormat fileFormat, String description, String parentId){
      AudioFormat format = fileFormat.getFormat();

      Content.Builder<Audio, AudioInputStream> builder = item.createContent(Audio.class)
        .withData(() -> audioInputStream)
        .withDescription(description)
        .withProperty(PropertyKeys.PROPERTY_KEY_PARENT, parentId)
        .withPropertyIf("channels", format.getChannels(), format.getChannels() != AudioSystem.NOT_SPECIFIED)
        .withProperty("encoding", format.getEncoding())
        .withPropertyIf("frameLength", fileFormat.getFrameLength(), fileFormat.getFrameLength() != AudioSystem.NOT_SPECIFIED)
        .withPropertyIf("frameRate", format.getFrameRate(), format.getFrameRate() != AudioSystem.NOT_SPECIFIED)
        .withPropertyIf("frameSize", format.getFrameSize(), format.getFrameSize() != AudioSystem.NOT_SPECIFIED)
        .withPropertyIf("sampleRate", format.getSampleRate(), format.getSampleRate() != AudioSystem.NOT_SPECIFIED)
        .withPropertyIf("sampleSize", format.getSampleSizeInBits(), format.getSampleSizeInBits() != AudioSystem.NOT_SPECIFIED)
        .withProperty(PropertyKeys.PROPERTY_KEY_TYPE, fileFormat.getType());

      for(Map.Entry<String, Object> e : fileFormat.properties().entrySet()){
        builder = builder.withProperty(e.getKey(), e.getValue());
      }
      for(Map.Entry<String, Object> e : format.properties().entrySet()){
        builder = builder.withProperty(e.getKey(), e.getValue());
      }

      builder.save();
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> fileExtensions = List.of("au", "aiff", "wav"); //TODO: What file types do we support? MP3? SND?
    private boolean discardOriginal = true;

    @Override
    public boolean validate() {
      return fileExtensions != null;
    }

    @Description("List of file extensions to accept when processing files")
    public List<String> getFileExtensions() {
      return fileExtensions;
    }

    public void setFileExtensions(List<String> fileExtensions) {
      this.fileExtensions = fileExtensions;
    }

    @Description("Should the original Content be discarded when audio is extracted?")
    public boolean isDiscardOriginal() {
      return discardOriginal;
    }

    public void setDiscardOriginal(boolean discardOriginal) {
      this.discardOriginal = discardOriginal;
    }
  }
}
