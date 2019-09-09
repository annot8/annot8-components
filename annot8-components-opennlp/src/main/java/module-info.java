module io.annot8.components.opennlp {
  requires transitive io.annot8.core;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires org.apache.opennlp.tools;

  exports io.annot8.components.opennlp.processors;
}
