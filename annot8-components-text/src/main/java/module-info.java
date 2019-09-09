open module io.annot8.components.text {
  requires transitive io.annot8.core;
  requires io.annot8.common.data;
  requires io.annot8.components.base;
  requires io.annot8.conventions;
  requires language.detector;

  exports io.annot8.components.text.processors;
  exports io.annot8.components.text.processors.settings;
}
