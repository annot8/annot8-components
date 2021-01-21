open module io.annot8.components.base {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.common.components;
  requires micrometer.core;
  requires io.annot8.conventions;

  exports io.annot8.components.base.processors;
  exports io.annot8.components.base.source;
  exports io.annot8.components.base.utils;
}
