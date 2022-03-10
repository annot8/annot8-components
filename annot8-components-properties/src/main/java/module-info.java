module io.annot8.components.properties {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.common.components;
  requires org.slf4j;
  requires jakarta.json.bind;
  requires io.annot8.components.base;
  requires io.annot8.conventions;

  exports io.annot8.components.properties.processors;
}
