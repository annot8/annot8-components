module io.annot8.components.mongo {
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires transitive mongo.java.driver;
  requires com.google.common;
  requires io.annot8.common.jackson.serialisation;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.core;
  requires io.annot8.common.implementations;

  exports io.annot8.components.mongo;
  exports io.annot8.components.mongo.data;
  exports io.annot8.components.mongo.processors;
  exports io.annot8.components.mongo.resources;
  exports io.annot8.components.mongo.sources;
}
