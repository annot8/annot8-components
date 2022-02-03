module io.annot8.components.files {
  requires transitive io.annot8.api;
  requires transitive io.annot8.common.data;
  requires io.annot8.conventions;
  requires io.annot8.common.components;
  requires transitive io.annot8.components.base;

  exports io.annot8.components.files.sources;
  exports io.annot8.components.files.processors;
  exports io.annot8.components.files.sinks;
  exports io.annot8.components.files.content;

  requires org.slf4j;
  requires apache.mime4j.dom;
  requires com.google.common;
  requires apache.mime4j.core;
  requires jakarta.json.bind;
  requires jakarta.json;
  requires java.desktop;
  requires org.apache.commons.text;
  requires de.siegmar.fastcsv;
  requires org.apache.commons.compress;
  requires transitive simplemagic;
}
