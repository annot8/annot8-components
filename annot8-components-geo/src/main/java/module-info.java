open module io.annot8.components.geo {
  requires transitive io.annot8.core;
  requires io.annot8.common.data;
  requires io.annot8.common.components;
  requires openlocationcode;
  requires geodesy;
  requires io.annot8.common.utils;
  requires java.json.bind;
  requires io.annot8.conventions;
  requires io.annot8.components.base;

  exports io.annot8.components.geo.processors;
}
