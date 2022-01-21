module io.annot8.components.wordnet {
  requires transitive io.annot8.api;
  requires transitive io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.common.components;
  requires io.annot8.conventions;
  requires transitive extjwnl;

  exports io.annot8.components.wordnet.processors;
}
