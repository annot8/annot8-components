module io.annot8.components.stopwords {
  requires transitive io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;

  exports io.annot8.components.stopwords.resources;
  exports io.annot8.components.stopwords.processors;
}
