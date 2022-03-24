/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.processors;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JdbcSettingsTest {

  private static final String USER = "USER";
  private static final String PASS = "PASS";

  @Test
  void testValidate() {
    JdbcSettings settings = new JdbcSettings("jdbc:sqlite://exampleDB.db");
    assertTrue(settings.validate());
  }

  @Test
  void testInvalidSettings() {
    JdbcSettings settings = new JdbcSettings(null);
    JdbcSettings emptySettings = new JdbcSettings("");
    assertFalse(settings.validate());
    assertFalse(emptySettings.validate());
  }

  @Test
  void testCredentials() {
    JdbcSettings settings = new JdbcSettings("jdbc::url", USER, PASS);
    assertEquals(USER, settings.getUser());
    assertEquals(PASS, settings.getPassword());
  }
}
