module io.annot8.components.annotations {
  requires transitive io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires jakarta.json.bind;
  requires org.slf4j;
  requires io.annot8.components.base;
  requires io.annot8.conventions;

  exports io.annot8.components.annotations.processors;
}
