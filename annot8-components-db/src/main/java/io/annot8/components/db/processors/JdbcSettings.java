/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.db.processors;

import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

public class JdbcSettings implements Settings {

  public static final int TIMEOUT = 1000;

  private final String jdbcUrl;
  private final String user;
  private final String password;

  public JdbcSettings(String jdbcUrl) {
    this(jdbcUrl, null, null);
  }

  @JsonbCreator
  public JdbcSettings(
      @JsonbProperty("jdbcUrl") String jdbcUrl,
      @JsonbProperty("user") String user,
      @JsonbProperty("password") String password) {
    this.jdbcUrl = jdbcUrl;
    this.user = user;
    this.password = password;
  }

  @Override
  public boolean validate() {
    return jdbcUrl != null && !jdbcUrl.isEmpty();
  }

  @Description("Valid JDBC URL")
  public String getJdbcUrl() {
    return jdbcUrl;
  }

  @Description("Username, or null if no password")
  public String getUser() {
    return user;
  }

  @Description("Password, or null if no password")
  public String getPassword() {
    return password;
  }
}
