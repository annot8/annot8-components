open module io.annot8.components.base {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.common.components;
  requires micrometer.core;

  exports io.annot8.components.base.processors;
  exports io.annot8.components.base.source;
}
