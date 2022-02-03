module io.annot8.components.gazetteers {
  requires transitive io.annot8.api;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires io.annot8.components.base.text;
  requires io.annot8.conventions;
  requires ahocorasick;
  requires evo.inflector;
  requires io.annot8.utils.text;
  requires de.siegmar.fastcsv;

  exports io.annot8.components.gazetteers.processors;
  exports io.annot8.components.gazetteers.processors.impl;
}
