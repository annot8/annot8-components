open module io.annot8.components.geo {
  requires transitive io.annot8.core;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires openlocationcode;
  requires geodesy;

  exports io.annot8.components.geo.processors;
}
