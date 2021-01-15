module annot8.components.kafka {
  requires io.annot8.common.data;
  requires io.annot8.conventions;
  requires io.annot8.common.components;
  requires kafka.clients;

  exports io.annot8.components.kafka.sources;
}
