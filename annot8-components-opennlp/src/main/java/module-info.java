module io.annot8.components.opennlp {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires org.apache.opennlp.tools;
  requires io.annot8.common.utils;
  requires io.annot8.common.components;

  exports io.annot8.components.opennlp.processors;
}
