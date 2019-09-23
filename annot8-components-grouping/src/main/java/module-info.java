open module io.annot8.components.grouping {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires com.google.common;
  requires io.annot8.common.components;

  exports io.annot8.components.grouping.processors;
}
