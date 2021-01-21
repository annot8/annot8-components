/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.elasticsearch;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.http.HttpHost;
import org.junit.jupiter.api.Test;

public class ElasticsearchSettingsTest {

  @Test
  public void testDefault() {
    ElasticsearchSettings es = new ElasticsearchSettings();

    assertTrue(es.validate());

    assertEquals("localhost", es.getHostname());
    assertEquals(9200, es.getPort());
    assertEquals("http", es.getScheme());
    assertEquals("baleen", es.getIndex());
    assertFalse(es.isDeleteIndex());
    assertFalse(es.isForceString());

    assertEquals(new HttpHost("localhost", 9200, "http"), es.host());
  }

  @Test
  public void testCustom() {
    ElasticsearchSettings es =
        new ElasticsearchSettings("myhost.com", 9090, "https", "test", true, true);

    assertTrue(es.validate());

    assertEquals("myhost.com", es.getHostname());
    assertEquals(9090, es.getPort());
    assertEquals("https", es.getScheme());
    assertEquals("test", es.getIndex());
    assertTrue(es.isDeleteIndex());
    assertTrue(es.isForceString());

    assertEquals(new HttpHost("myhost.com", 9090, "https"), es.host());
  }

  @Test
  public void testSetters() {
    ElasticsearchSettings es = new ElasticsearchSettings();

    es.setHostname("myhost.com");
    assertEquals("myhost.com", es.getHostname());

    es.setPort(9090);
    assertEquals(9090, es.getPort());

    es.setScheme("https");
    assertEquals("https", es.getScheme());

    es.setIndex("test");
    assertEquals("test", es.getIndex());

    es.setDeleteIndex(true);
    assertTrue(es.isDeleteIndex());

    es.setForceString(true);
    assertTrue(es.isForceString());
  }
}
