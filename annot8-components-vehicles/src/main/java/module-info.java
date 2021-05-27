module io.annot8.components.vehicles {
  requires io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires io.annot8.components.stopwords;
  requires jakarta.json.bind;
  requires io.annot8.utils.text;
  requires io.annot8.components.base;

  exports io.annot8.components.vehicles.processors;
}
