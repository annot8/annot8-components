/** Components for translating content and other similar language related tasks */
open module io.annot8.components.translation {
  requires transitive io.annot8.api;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires io.annot8.common.components;

  requires connector.api;

  exports io.annot8.components.translation.processors;
}
