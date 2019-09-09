/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.processors;

import io.annot8.core.settings.Settings;

public class JDBCSettings implements Settings {

  public static final int TIMEOUT = 1000;

  private final String jdbcUrl;
  private final String user;
  private final String password;

  public JDBCSettings(String jdbcUrl) {
    this(jdbcUrl, null, null);
  }

  public JDBCSettings(String jdbcUrl, String user, String password) {
    this.jdbcUrl = jdbcUrl;
    this.user = user;
    this.password = password;
  }

  @Override
  public boolean validate() {
    return jdbcUrl != null && !jdbcUrl.isEmpty();
  }

  public String getJdbcUrl() {
    return jdbcUrl;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
