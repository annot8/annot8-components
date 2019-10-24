module io.annot8.components.db {
  requires io.annot8.conventions;
  requires transitive io.annot8.components.base;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires com.google.common;
  requires sqlite.jdbc;
  requires java.sql;
  requires java.json.bind;

  exports io.annot8.components.db.content;
  exports io.annot8.components.db.processors;
}
