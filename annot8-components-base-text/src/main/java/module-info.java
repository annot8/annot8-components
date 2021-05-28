module io.annot8.components.base.text {
  requires io.annot8.common.data;
  requires io.annot8.common.components;
  requires io.annot8.components.base;
  requires io.annot8.components.stopwords;
  requires io.annot8.conventions;
  requires jakarta.json.bind;

  exports io.annot8.components.base.text.processors;
}
