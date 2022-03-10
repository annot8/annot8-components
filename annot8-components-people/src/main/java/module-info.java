module io.annot8.components.people {
  requires transitive io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires jakarta.json.bind;
  requires io.annot8.components.base;

  exports io.annot8.components.people.processors;
}
