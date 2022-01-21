module io.annot8.components.comms {
  requires transitive io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires transitive libphonenumber;

  exports io.annot8.components.comms.processors;
}
