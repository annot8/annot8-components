module io.annot8.components.documents {
  exports io.annot8.components.documents.processors;

  requires transitive io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.conventions;
  requires java.desktop;
  requires metadata.extractor;
  requires odfdom.java;
  requires org.apache.pdfbox;
  requires org.jsoup;
  requires org.slf4j;
  requires org.apache.poi.poi;
  requires org.apache.poi.ooxml;
  requires org.apache.poi.scratchpad;
  requires org.apache.commons.compress;
  requires java.net.http;
  requires javatuples;
}
