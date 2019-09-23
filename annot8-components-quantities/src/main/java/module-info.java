open module io.annot8.components.quantities {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.conventions;
  requires transitive io.annot8.components.base;
  requires org.slf4j;
  requires io.annot8.common.components;

  exports io.annot8.components.quantities.processors;
}
