open module io.annot8.components.geo {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.common.components;
  requires openlocationcode;
  requires geodesy;
  requires io.annot8.common.utils;
  requires java.json.bind;
  requires io.annot8.conventions;
  requires io.annot8.components.base.text;
  requires java.json;
  requires io.annot8.components.gazetteers;
  requires uk.gov.dstl.geo.osgb;
  requires opencsv;

  exports io.annot8.components.geo.processors;
}
