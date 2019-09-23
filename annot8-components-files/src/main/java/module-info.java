module io.annot8.components.files {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.conventions;
  requires io.annot8.common.components;
  requires transitive io.annot8.components.base;

  exports io.annot8.components.files.sources;
  exports io.annot8.components.files.processors;

  requires org.slf4j;
  requires apache.mime4j.dom;
  requires com.google.common;
  requires apache.mime4j.core;
  requires java.json.bind;
}
