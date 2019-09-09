module io.annot8.components.monitor {
  requires transitive io.annot8.core;
  requires org.slf4j;
  requires micrometer.core;

  exports io.annot8.components.monitor.resources;
  exports io.annot8.components.monitor.resources.metering;
}
