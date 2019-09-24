open module io.annot8.components.base {
  requires transitive io.annot8.api;
  requires org.slf4j;
  requires io.annot8.common.data;
  requires io.annot8.common.utils;
  requires io.annot8.common.components;
  requires com.google.common;
  requires java.json.bind;

  exports io.annot8.components.base.processors;
}
