open module io.annot8.components.financial {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires iban4j;
  requires io.annot8.common.components;
  requires org.bitcoinj.core;

  exports io.annot8.components.financial.processors;
}
