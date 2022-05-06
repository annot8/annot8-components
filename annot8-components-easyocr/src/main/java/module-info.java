module io.annot8.components.easyocr {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires transitive io.annot8.common.components;
  requires org.slf4j;
  requires java.desktop;
  requires io.annot8.conventions;
  requires java.net.http;
  requires com.fasterxml.jackson.databind;

  exports io.annot8.components.easyocr.processors;
}
