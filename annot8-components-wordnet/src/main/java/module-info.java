module io.annot8.components.wordnet {
  requires io.annot8.api;
  requires java.json.bind;
  requires extjwnl;
  requires extjwnl.data.wn31;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;

  exports io.annot8.components.wordnet.processors;
}
