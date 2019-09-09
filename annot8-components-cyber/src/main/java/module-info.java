open module io.annot8.components.cyber {
  requires transitive io.annot8.core;
  requires io.annot8.common.data;
  requires transitive io.annot8.components.base;
  requires io.annot8.conventions;
  requires com.google.common;

  exports io.annot8.components.cyber.processors;
}
