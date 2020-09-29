open module io.annot8.components.text {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires lingua;
  requires io.annot8.common.components;
  requires java.json.bind;

  exports io.annot8.components.text.processors;
}
