module annot8.components.kafka {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.conventions;
  requires io.annot8.common.components;
  requires transitive kafka.clients;

  exports io.annot8.components.kafka.sources;
}
