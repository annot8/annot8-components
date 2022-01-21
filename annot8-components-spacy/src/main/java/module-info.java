module io.annot8.components.spacy {
  requires transitive io.annot8.api;
  requires transitive io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires io.annot8.common.utils;
  requires io.annot8.common.components;
  requires io.annot8.components.base;
  requires com.fasterxml.jackson.annotation;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires java.compiler;
  requires java.net.http;
  requires java.ws.rs;
  requires java.xml.bind;
  requires swagger.annotations;
  requires jackson.databind.nullable;
  requires com.fasterxml.jackson.datatype.jsr310;
  requires jsr305;

  exports io.annot8.components.spacy.processors;
}
