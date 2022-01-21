module io.annot8.components.db {
  requires transitive io.annot8.components.base;
  requires io.annot8.common.components;
  requires transitive io.annot8.common.data;
  requires com.google.common;
  requires sqlite.jdbc;
  requires java.sql;
  requires jakarta.json.bind;

  exports io.annot8.components.db.content;
  exports io.annot8.components.db.processors;
}
