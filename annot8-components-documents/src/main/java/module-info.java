module io.annot8.components.documents {
  exports io.annot8.components.documents.processors;

  requires transitive io.annot8.api;
  requires io.annot8.common.components;
  requires transitive io.annot8.common.data;
  requires io.annot8.common.utils;
  requires io.annot8.conventions;
  requires transitive java.desktop;
  requires metadata.extractor;
  requires odfdom.java;
  requires org.apache.pdfbox;
  requires transitive org.jsoup;
  requires org.slf4j;
  requires org.apache.poi.poi;
  requires transitive org.apache.poi.ooxml;
  requires transitive org.apache.poi.scratchpad;
  requires org.apache.commons.compress;
  requires java.net.http;
  requires javatuples;
  requires tabula;
}
