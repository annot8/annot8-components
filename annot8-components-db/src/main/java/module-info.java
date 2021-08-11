module io.annot8.components.db {
  requires transitive io.annot8.components.base;
  requires io.annot8.common.components;
  requires io.annot8.common.data;
  requires com.google.common;
  requires org.xerial.sqlitejdbc;
  requires java.sql;
  requires jakarta.json.bind;

  exports io.annot8.components.db.content;
  exports io.annot8.components.db.processors;
}
