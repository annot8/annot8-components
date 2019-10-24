open module io.annot8.components.base {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.common.utils;
  requires io.annot8.common.components;
  requires com.google.common;
  requires java.json.bind;
  requires io.annot8.conventions;
  requires io.annot8.components.stopwords;

  exports io.annot8.components.base.processors;
  exports io.annot8.components.base.source;
}
