open module io.annot8.components.cyber {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires transitive io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires com.google.common;
  requires io.annot8.common.components;
  requires jakarta.json.bind;

  exports io.annot8.components.cyber.processors;
}
