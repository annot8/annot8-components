module io.annot8.components.audio {
  requires io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.conventions;
  requires java.desktop;
  requires com.google.gson;
  requires vosk;

  exports io.annot8.components.audio.processors;
}
