module io.annot8.components.image {
  requires transitive io.annot8.api;
  requires io.annot8.components.base;
  requires transitive io.annot8.common.data;
  requires metadata.extractor;
  requires io.annot8.common.components;
  requires io.annot8.conventions;
  requires java.desktop;

  exports io.annot8.components.image.processors;
}
