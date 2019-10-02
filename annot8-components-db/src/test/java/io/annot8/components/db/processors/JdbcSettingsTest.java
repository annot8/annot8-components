/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.processors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class JdbcSettingsTest {

  private static final String JDBC_TEST_URL = "jdbc:hsqldb:hsql://localhost:9001/test";
  private static final String USER = "TEST";
  private static final String PASS = "TEST";

  @Test
  public void testValidate() {
    JdbcSettings settings = new JdbcSettings("jdbc:sqlite://exampleDB.db");
    assertTrue(settings.validate());
  }

  @Test
  public void testInvalidSettings() {
    JdbcSettings settings = new JdbcSettings(null);
    JdbcSettings emptySettings = new JdbcSettings("");
    assertFalse(settings.validate());
    assertFalse(emptySettings.validate());
  }

  @Test
  public void testCredentials() {
    String user = "user";
    String pass = "pass";
    JdbcSettings settings = new JdbcSettings("jdbc::url", user, pass);
    assertEquals(user, settings.getUser());
    assertEquals(pass, settings.getPassword());
  }
}
