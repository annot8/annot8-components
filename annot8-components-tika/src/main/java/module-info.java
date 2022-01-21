module io.annot8.components.tika {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.common.components;
  requires java.xml;
  requires tika.core;

  exports io.annot8.components.tika.processors;
}
