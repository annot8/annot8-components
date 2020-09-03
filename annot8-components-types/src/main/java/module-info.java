open module io.annot8.components.types {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires io.annot8.common.components;
  requires java.json.bind;
  requires org.slf4j;

  exports io.annot8.components.types.processors;
}
