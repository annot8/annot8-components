module io.annot8.components.opencv {
  requires transitive io.annot8.api;
  requires transitive io.annot8.common.data;
  requires io.annot8.conventions;
  requires io.annot8.common.components;
  requires java.desktop;
  requires opencv;

  exports io.annot8.components.opencv.processors;
}
