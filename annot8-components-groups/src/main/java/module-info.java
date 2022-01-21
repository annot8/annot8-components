module io.annot8.components.groups {
  requires transitive io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires jakarta.json.bind;
  requires org.slf4j;

  exports io.annot8.components.groups.processors;
}
