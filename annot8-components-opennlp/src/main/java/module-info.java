module io.annot8.components.opennlp {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires org.apache.opennlp.tools;
  requires io.annot8.common.utils;

  exports io.annot8.components.opennlp.processors;
}
