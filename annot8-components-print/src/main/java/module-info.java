open module io.annot8.components.print {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.common.components;
  requires io.annot8.components.base.text;
  requires transitive org.slf4j;

  exports io.annot8.components.print.processors;
}
