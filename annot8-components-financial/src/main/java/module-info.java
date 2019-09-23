open module io.annot8.components.financial {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires iban4j;
  requires bitcoinj.core;
  requires io.annot8.common.components;

  exports io.annot8.components.financial.processors;
}
