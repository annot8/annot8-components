module uk.gov.dstl.annot8.processors.vehicle {
  requires io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires io.annot8.components.stopwords;

  requires java.json.bind;
  requires io.annot8.utils.text;

  exports io.annot8.components.vehicles.processors;
}