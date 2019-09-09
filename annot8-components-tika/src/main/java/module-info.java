module io.annot8.components.tika {
  requires transitive io.annot8.core;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires java.xml;
  requires org.apache.tika.core;
  requires org.apache.tika.parsers;

  exports io.annot8.components.tika.processors;
}
