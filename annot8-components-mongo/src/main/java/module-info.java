module io.annot8.components.mongo {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires com.google.common;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.core;
  requires transitive io.annot8.implementations.support;
  requires io.annot8.common.components;
  requires transitive org.mongodb.bson;
  requires org.mongodb.driver.core;
  requires org.mongodb.driver.sync.client;

  exports io.annot8.components.mongo;
  exports io.annot8.components.mongo.data;
  exports io.annot8.components.mongo.processors;
  exports io.annot8.components.mongo.resources;
  exports io.annot8.components.mongo.sources;
}
