open module io.annot8.components.base {
  requires transitive io.annot8.core;
  requires org.slf4j;
  requires io.annot8.common.data;
  requires io.annot8.common.utils;
  requires io.annot8.common.components;
  requires com.google.common;

  exports io.annot8.components.base.processors;
}
